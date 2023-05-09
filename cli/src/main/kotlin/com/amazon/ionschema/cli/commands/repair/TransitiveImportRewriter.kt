package com.amazon.ionschema.cli.commands.repair

import com.amazon.ion.IonSystem
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionelement.api.AnyElement
import com.amazon.ionelement.api.IonElementLoaderOptions
import com.amazon.ionelement.api.IonLocation
import com.amazon.ionelement.api.IonTextLocation
import com.amazon.ionelement.api.StructElement
import com.amazon.ionelement.api.TextElement
import com.amazon.ionelement.api.ionString
import com.amazon.ionelement.api.ionStructOf
import com.amazon.ionelement.api.ionSymbol
import com.amazon.ionelement.api.loadAllElements
import com.amazon.ionelement.api.location
import com.amazon.ionelement.api.toIonElement
import com.amazon.ionschema.Authority
import com.amazon.ionschema.AuthorityFilesystem
import com.amazon.ionschema.IonSchemaSystem
import com.amazon.ionschema.IonSchemaSystemBuilder
import com.amazon.ionschema.Schema
import com.amazon.ionschema.cli.util.PatchSet
import com.amazon.ionschema.cli.util.PreOrder
import com.amazon.ionschema.cli.util.createImportForType
import com.amazon.ionschema.cli.util.findTypeReferences
import com.amazon.ionschema.cli.util.forEachSchemaInPath
import com.amazon.ionschema.cli.util.includesTypeImport
import com.amazon.ionschema.cli.util.inferIndent
import com.amazon.ionschema.cli.util.isBuiltInTypeName
import com.amazon.ionschema.cli.util.recursivelyVisit
import com.amazon.ionschema.cli.util.rewriteFile
import java.io.File
import java.time.Instant
import java.util.Comparator

class TransitiveImportRewriter(
    private val importStrategy: ImportStrategy,
    private val ionSystem: IonSystem = IonSystemBuilder.standard().build(),
    private val skipCleanUp: Boolean,
    private val echo: (String) -> Unit = {},
) {

    companion object {
        /**
         * Compares header import structs lexicographically by schemaId, then by type name, then by alias.
         * Imports with no type name come before imports with a type name.
         * Imports with no alias come before imports with an alias.
         */
        private val importComparator = Comparator.comparing<StructElement?, String?> { it["id"].textValue }
            .thenBy { it.getOptional("type")?.textValue }
            .thenBy { it.getOptional("as")?.textValue }
    }

    enum class ImportStrategy {
        /**
         * While fixing imports, rewrite all imports as `{ id: <SCHEMA_ID>, type: <TYPE_NAME> } `.
         * You might find this style annoying, and it will possibly generate larger diff.
         * However, it is guaranteed to never have name conflicts unintentionally introduced by a dependency in the future.
         */
        NoWildcards,
        /**
         * While fixing imports, keeps all existing "wildcard" imports (i.e. `{ id: <SCHEMA_ID> }`), but all new imports
         * it adds will be type imports (i.e. `{ id: <SCHEMA_ID>, type: <TYPE_NAME> }`).
         * This strategy will introduce smaller changes, and will not introduce any naming conflicts.
         * However, the presence of wildcard imports means that unintentional name conflicts could happen in the future.
         */
        KeepWildcards,

        /**
         * While fixing imports, use only wildcard imports in the header, unless a type-specific import is required for
         * a type alias. It is unlikely but possible that this strategy could introduce name conflicts, rendering the
         * schema invalid. However, this tool will detect that during the final validation and throw an exception.
         * If this strategy results in invalid schemas, use [KeepWildcards] or [NoWildcards] instead.
         */
        PreferWildcards,
    }

    /**
     * Logic:
     *
     * ### Phase 0:
     *
     * Ensure all schemas load correctly using transitive imports before starting to fix the imports.
     *
     * ### Phase 1:
     *
     * For each file in base path, if it is an aggregator/export schema, rewrite it in temp directory, otherwise copy to
     * temp directory unchanged.
     *
     * An "aggregator/export" schema is a schema that declares no types, but has types that are imported in the header.
     * The only purpose for such a schema is to transitively re-export the types it imports—essentially a public API for
     * a collection of schemas. We want to preserve the functionality of these schemas and any schemas that depend on
     * them, so we rewrite these schemas first, and then rewrite all other schemas afterwards so that they can correctly
     * resolve imports to these schemas.
     *
     * ### Phase 2:
     *
     * For each file in base path, if is an aggregator/export schema, copy to temp directory. Otherwise, check and
     * resolve all header imports and inline imports. Also removes unused imports.
     *
     * ### Phase 3:
     *
     * Read all schemas in the new base path to make sure that they all load correctly, and then clean up all
     * intermediate work.
     */
    fun fixTransitiveImports(basePath: String, newBasePath: String, authorities: List<Authority>) {
        fun newIonSchemaSystem(basePath: String, withTransitiveImports: Boolean) = IonSchemaSystemBuilder.standard()
            .allowTransitiveImports(withTransitiveImports)
            .withIonSystem(ionSystem)
            .withAuthorities(authorities)
            .withAuthority(AuthorityFilesystem(basePath))
            .build()

        echo("Phase 0: Validate original schemas")
        val issPhase0 = newIonSchemaSystem(basePath, withTransitiveImports = true)
        forEachSchemaInPath(basePath) { id, _ -> issPhase0.loadSchema(id) }
            .also(reportErrors("validate"))

        echo("Phase 1: Rewrite aggregating schemas")
        val newBasePathPhase1 = "$newBasePath/._rewriter_phase1_${Instant.now()}"
        val issPhase1 = newIonSchemaSystem(basePath, withTransitiveImports = true)
        forEachSchemaInPath(basePath) { schemaId, file ->
            val patches = rewriteAggregatingSchema(schemaId, file, issPhase1)
            rewriteFile(file, basePath, newBasePathPhase1, patches)
        }.also(reportErrors("rewrite"))

        echo("Phase 2: Rewrite all other schemas")
        val newBasePathPhase2 = "$newBasePath/._rewriter_phase2_${Instant.now()}"
        val issPhase2 = newIonSchemaSystem(newBasePathPhase1, withTransitiveImports = true)
        forEachSchemaInPath(newBasePathPhase1) { schemaId, file ->
            val patches = rewriteStandardSchema(schemaId, file, issPhase2)
            rewriteFile(file, newBasePathPhase1, newBasePathPhase2, patches)
        }.also(reportErrors("rewrite"))

        echo("Phase 3: Validate new schemas")
        val issPhase3 = newIonSchemaSystem(newBasePathPhase2, withTransitiveImports = false)
        forEachSchemaInPath(newBasePathPhase2) { id, _ -> issPhase3.loadSchema(id) }
            .also(reportErrors("validate"))

        File(newBasePathPhase2).copyRecursively(File(newBasePath))
        if (!skipCleanUp) {
            File(newBasePathPhase1).deleteRecursively()
            File(newBasePathPhase2).deleteRecursively()
        }
    }

    private fun reportErrors(attemptedAction: String): (List<Pair<String, Throwable>>) -> Unit = { errs ->
        errs.forEach { (s, t) -> echo("[\n  $s,\n  ${t.stackTraceToString()}\n]") }
        if (errs.isNotEmpty()) {
            val failureList = errs.joinToString("") { (k, v) -> "\n  [$k] ${v.message}," }
            throw Exception("Failed to $attemptedAction:$failureList")
        }
    }

    /**
     * Replaces every type that is imported in the header with an explicit re-export of the type so that consumers of
     * this schema can still access all the types without using transitive imports. I.e.:
     * ```
     * type::{ name:<ALIAS>, type: { id:<SCHEMA_ID>, type:<TYPE_NAME> } }
     * ```
     * See template in function body for details.
     */
    private fun rewriteAggregatingSchema(schemaId: String, schemaFile: File, iss: IonSchemaSystem): PatchSet {
        val patchSet = PatchSet()
        val schemaIslString = schemaFile.readText()

        val schema = iss.newSchema(schemaIslString)
        if (!isExportOnlySchema(schema)) return patchSet

        val exports = schema.getTypes().asSequence()
            .filter { !isBuiltInTypeName(it.name) && it.schemaId != null }
            .map {
                val alias = it.name
                val name = it.isl.toIonElement().asStruct()["name"].textValue
                val importedFromSchemaId = it.schemaId!!
                ionStructOf(
                    // alias and name could be the same value if there was no import alias
                    // in which case it is being re-exported as the same name.
                    "name" to ionSymbol(alias),
                    "type" to ionStructOf(
                        "id" to ionString(importedFromSchemaId),
                        "type" to ionSymbol(name)
                    ),
                    annotations = listOf("type")
                )
            }
            .sortedBy { it["name"].textValue }
            .joinToString("\n")

        patchSet.replaceAll(
            """
            |${'$'}ion_schema_1_0
            |
            |// Schema '$schemaId'
            |// 
            |// The purpose of this schema is to decouple consumers of the schema from the
            |// implementation details (ie. specific locations) of each type that it provides,
            |// and to indicate to consumers, which types they SHOULD use. Consumers of this
            |// type CAN bypass this schema and import other types directly, but they SHOULD NOT
            |// unless directed to do so by the owner(s)/author(s) of this schema.
            |//
            |// The type
            |//     type::{name:foobar,type:{id:"bar.isl",type:foo}}
            |// is analogous to
            |//   [Javascript]: export { foo as foobar } from 'bar.isl'
            |//         [Rust]: pub use bar::foo as foobar;
            |
            |
            |$exports
            |
            """.trimMargin()
        )
        return patchSet
    }

    /**
     * Checks all header imports and inline imports, resolving them to be direct imports instead of transitive imports.
     */
    private fun rewriteStandardSchema(schemaId: String, schemaFile: File, iss: IonSchemaSystem): PatchSet {
        val patchSet = PatchSet()
        val schema = iss.loadSchema(schemaId)

        if (isExportOnlySchema(schema)) return patchSet

        val headerImports: List<StructElement> = schema.isl
            .singleOrNull { it.hasTypeAnnotation("schema_header") }?.toIonElement()
            ?.asStruct()?.getOptional("imports")?.asList()?.values?.map { it.asStruct() }
            ?: emptyList()

        // Determine changes
        val newHeaderImports = calculateNewHeaderImports(schema).sortedWith(importComparator)
        val oldInlineImportsToNewInlineImportsMap = calculateNewInlineImportMapping(schemaId, iss)

        // If no header and no changes to inline imports, do nothing
        if (headerImports.isEmpty() && oldInlineImportsToNewInlineImportsMap.isEmpty()) {
            return patchSet
        }

        // Generate Patch Set

        // In order to make the least destructive possible diff, we load the Schema as raw Ion using the IonElement API.
        // We find the text location of the values that need to be updated, and then apply the change to the original
        // text of the file rather than modifying and writing the IonElement DOM. This allows us to preserve almost all
        // comments and formatting.

        val schemaIslString = schemaFile.readText()
        val schemaIonElements = loadAllElements(schemaIslString, IonElementLoaderOptions(includeLocationMeta = true))

        // IonElement provides row/col positions; get information to compute string index from row/col
        val newlineLocations = schemaIslString.mapIndexedNotNull { i, c -> if (c == '\n') i else null }
        val lineStartLocations = (listOf(0) + newlineLocations.map { it + 1 })
        fun IonLocation?.toIndex(): Int = with(this as IonTextLocation) {
            lineStartLocations[line.toInt() - 1] + charOffset.toInt() - 1
        }

        if (newHeaderImports != headerImports) {
            schemaIonElements.singleOrNull { "schema_header" in it.annotations }
                ?.let {
                    val imports = it.asStruct().getOptional("imports")?.asList() ?: return@let
                    if (imports.values.isEmpty()) return@let

                    val importListStartLocation =
                        schemaIslString.indexOf('[', startIndex = imports.metas.location.toIndex()) + 1
                    val importListEndLocation =
                        schemaIslString.indexOf(']', startIndex = imports.values.last().metas.location.toIndex()) - 1

                    val replacementText = if (newHeaderImports.isEmpty()) {
                        ""
                    } else {
                        val indent = schemaIslString.inferIndent() ?: "  "
                        newHeaderImports.joinToString(separator = "") { "\n$indent$indent$it," } + "\n$indent"
                    }
                    patchSet.patch(importListStartLocation, importListEndLocation, replacementText)
                }
        }

        // Patching the inline imports is a little easier. We will write the entire struct on a single line regardless
        // of how they were first written so that we don't need to figure out the correct indentation.
        val inlineImportPatchingVisitor = visitor@{ it: AnyElement ->
            if (it is StructElement && oldInlineImportsToNewInlineImportsMap.containsKey(it) && oldInlineImportsToNewInlineImportsMap[it] != it) {
                val start = schemaIslString.indexOf('{', startIndex = it.metas.location.toIndex())
                val end = schemaIslString.indexOf("}", startIndex = start)
                val replacementText = oldInlineImportsToNewInlineImportsMap[it].toString()
                patchSet.patch(start, end, replacementText)
            }
        }
        schemaIonElements.forEach { it.recursivelyVisit(PreOrder, inlineImportPatchingVisitor) }

        return patchSet
    }

    /**
     * Determines the new header imports for a schema.
     */
    private fun calculateNewHeaderImports(schema: Schema): List<StructElement> {
        val headerImports: List<StructElement> = schema.isl
            .singleOrNull { it.hasTypeAnnotation("schema_header") }?.toIonElement()
            ?.asStruct()?.getOptional("imports")?.asList()?.values?.map { it.asStruct() }
            ?: emptyList()

        if (headerImports.isEmpty()) {
            return emptyList()
        }

        // find all type arguments in the schema that are type names.
        // If-and-only-if it is imported, create an import for it.
        val actualImportedTypes = schema.isl
            .filter { it.hasTypeAnnotation("type") }
            .flatMap { findTypeReferences(it.toIonElement()) }
            .asSequence()
            .filterIsInstance<TextElement>()
            .distinctBy { it.textValue }
            // If it's declared in the same schema, we don't need to import it
            .filter { schema.getDeclaredType(it.textValue) == null }
            // If it's a built-in type, we don't need to import it
            .filter { !isBuiltInTypeName(it.textValue) }
            .map { schema.getType(it.textValue)!! }
            .map { createImportForType(it) }
            .toList()

        return reconcileHeaderImports(headerImports, actualImportedTypes)
    }

    /**
     * Returns a map of inline imports that need to be replaced, mapped to their replacement inline imports
     */
    private fun calculateNewInlineImportMapping(schemaId: String, iss: IonSchemaSystem): Map<StructElement, StructElement> {
        val schema = iss.loadSchema(schemaId)
        return schema.isl
            .filter { it.hasTypeAnnotation("type") }
            .flatMap { findTypeReferences(it.toIonElement()) }
            .filterIsInstance<StructElement>() // Inline imports are structs, all other references are symbols
            .associateWith {
                val importedType = iss.loadSchema(it["id"].textValue).getType(it["type"].textValue)!!
                createImportForType(importedType)
            }
            .filter { (old, new) -> old != new } // remove any identity transforms
    }

    /**
     * Checks if a schema has at least one import, and has no declared types. I.e. its only purpose is to re-export other types.
     */
    private fun isExportOnlySchema(schema: Schema): Boolean {
        return schema.getDeclaredTypes().asSequence().toList().isEmpty() &&
            schema.getImports().hasNext()
    }

    /**
     * Computes the minimal set of header imports for the actual imported types, given the Import reconciliation strategy.
     */
    private fun reconcileHeaderImports(headerImports: List<StructElement>, actualImportedTypes: List<StructElement>): List<StructElement> {
        val newImports = mutableSetOf<StructElement>()
        when (importStrategy) {
            ImportStrategy.NoWildcards -> newImports.addAll(actualImportedTypes)
            ImportStrategy.KeepWildcards -> {
                headerImports.forEach { import ->
                    if (actualImportedTypes.any { import includesTypeImport it } && import !in newImports) {
                        newImports.add(import)
                    }
                }
                actualImportedTypes.forEach { typeImport ->
                    if (!newImports.any { it includesTypeImport typeImport } && typeImport !in newImports) {
                        newImports.add(typeImport)
                    }
                }
            }
            ImportStrategy.PreferWildcards -> {
                actualImportedTypes.forEach { typeImport ->
                    if (typeImport.getOptional("as") != null) {
                        newImports.add(typeImport)
                    } else {
                        val wildcard = ionStructOf("id" to typeImport["id"])
                        if (wildcard !in newImports) newImports.add(wildcard)
                    }
                }
            }
        }
        return newImports.toList()
    }
}
