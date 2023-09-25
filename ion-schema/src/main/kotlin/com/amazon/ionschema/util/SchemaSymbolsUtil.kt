package com.amazon.ionschema.util

import com.amazon.ion.IonContainer
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ionschema.AuthorityFilesystem
import com.amazon.ionschema.IonSchemaSystemBuilder
import com.amazon.ionschema.Schema
import com.amazon.ionschema.Type
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.SchemaDocument
import com.amazon.ionschema.model.TypeArgument
import com.amazon.ionschema.model.TypeDefinition
import com.amazon.ionschema.model.ValidValue
import com.amazon.ionschema.reader.IonSchemaReaderV2_0
import java.io.File

/**
 * Utility functions for getting all the symbol texts that are "expected" by Ion Schema types.
 *
 * By "expected", we refer to field names, annotations, and symbol literals that are included in the type definition.
 *
 * Consider this example type:
 * ```
 * type::{
 *   name: foo_type,
 *   annotations: closed::[foo],
 *   fields: {
 *     a: int,
 *     b: { valid_values: [1, true, yes] },
 *     c: { type: symbol, codepoint_length: range::[1, 10] },
 *     haystack: { type: list, contains: [needle] },
 *   }
 * }
 * ```
 * Getting the symbol texts for this type would return the set of `foo`, `a`, `b`, `c`, `yes`, `haystack`, and `needle`.
 *
 * Currently supports only Ion Schema 2.0.
 */
object SchemaSymbolsUtil {
    /**
     * Gets all symbol texts that are defined in all schemas in the given base path for an AuthorityFileSystem.
     */
    @JvmStatic
    @JvmOverloads
    fun getSymbolsTextForPath(basePath: String, fileFilter: (File) -> Boolean = { it.path.endsWith(".isl") }): Set<String> {
        val iss = IonSchemaSystemBuilder.standard()
            .withAuthority(AuthorityFilesystem(basePath))
            .allowTransitiveImports(false)
            .build()

        val baseDir = File(basePath)

        return baseDir.walk()
            .filter { it.isFile && fileFilter(it) }
            .map { getSymbolsTextForSchema(iss.loadSchema(it.relativeTo(baseDir).path)) }
            .flatten()
            .toSet()
    }

    /**
     * Gets all symbol texts for data that matches a [Type] that is declared in this [Schema].
     * This does _not_ resolve imports.
     */
    @JvmStatic
    @OptIn(ExperimentalIonSchemaModel::class)
    fun getSymbolsTextForSchema(schema: Schema): Set<String> {
        return IonSchemaReaderV2_0().readSchemaOrThrow(schema.isl).getAllSymbolsText()
    }

    /**
     * Gets all symbol texts for data that matches a [TypeDefinition] in this [SchemaDocument]. Only the symbols that are
     * locally defined will be included (i.e. imports are not resolved).
     */
    @OptIn(ExperimentalIonSchemaModel::class)
    private fun SchemaDocument.getAllSymbolsText(): Set<String> {
        return declaredTypes.values.flatMapTo(mutableSetOf()) {
            it.typeDefinition.getAllSymbolsText()
        }
    }

    /**
     * Gets all symbol texts for data that matches this [TypeDefinition].
     *
     * Caveat—this _does_ include anything in a `not` constraint, so constructs like `{not:{valid_values:[foo]}}` may
     * yield unexpected results.
     *
     * TODO: See if it's possible to track the `not`-depth to see whether the symbols in this [TypeDefinition] should be
     * excluded or included in the final result.
     */
    @OptIn(ExperimentalIonSchemaModel::class)
    private fun TypeDefinition.getAllSymbolsText(): Set<String> {
        val symbolText = mutableSetOf<String>()
        for (c in constraints) {
            when (c) {
                is Constraint.Fields -> c.fields.forEach { (fieldName, fieldType) ->
                    symbolText.add(fieldName)
                    symbolText.addAll(fieldType.typeArg.getAllSymbolsText())
                }
                is Constraint.ValidValues -> c.values.filterIsInstance<ValidValue.Value>()
                    .forEach { symbolText.addAll(it.value.getAllSymbolTexts()) }
                is Constraint.Contains -> c.values.forEach { symbolText.addAll(it.getAllSymbolTexts()) }
                is Constraint.AnnotationsV1 -> symbolText.addAll(c.annotations.map { it.text })
                is Constraint.AnnotationsV2.Standard -> symbolText.addAll(c.type.getAllSymbolsText())
                is Constraint.AnnotationsV2.Simplified -> symbolText.addAll(c.annotations)
                is Constraint.FieldNames -> symbolText.addAll(c.type.getAllSymbolsText())
                is Constraint.Type -> symbolText.addAll(c.type.getAllSymbolsText())
                is Constraint.Not -> symbolText.addAll(c.type.getAllSymbolsText())
                is Constraint.Element -> symbolText.addAll(c.type.getAllSymbolsText())
                is Constraint.OrderedElements -> c.types.forEach { symbolText.addAll(it.typeArg.getAllSymbolsText()) }
                is Constraint.AnyOf -> c.types.forEach { symbolText.addAll(it.getAllSymbolsText()) }
                is Constraint.AllOf -> c.types.forEach { symbolText.addAll(it.getAllSymbolsText()) }
                is Constraint.OneOf -> c.types.forEach { symbolText.addAll(it.getAllSymbolsText()) }
            }
        }
        return symbolText
    }

    @OptIn(ExperimentalIonSchemaModel::class)
    private fun TypeArgument.getAllSymbolsText(): Set<String> {
        return when (this) {
            is TypeArgument.InlineType -> typeDefinition.getAllSymbolsText()
            else -> emptySet()
        }
    }

    /**
     * Gets all symbol texts from a given IonValue. This includes all annotations, field names, and symbol values.
     */
    private fun IonValue.getAllSymbolTexts(): Set<String> {
        val symbolTexts = mutableSetOf<String>()
        symbolTexts.addAll(this.typeAnnotations)
        if (!this.isNullValue) when (this) {
            is IonSymbol -> symbolTexts.add(this.stringValue())
            is IonContainer -> {
                forEach {
                    val fName = it.fieldName
                    if (fName != null) symbolTexts.add(fName)
                    symbolTexts.addAll(it.getAllSymbolTexts())
                }
            }
        }
        return symbolTexts
    }
}
