/*
 * Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazon.ionschema.internal

import com.amazon.ion.IonDatagram
import com.amazon.ion.IonList
import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonText
import com.amazon.ion.IonValue
import com.amazon.ionschema.Import
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.Schema
import com.amazon.ionschema.Type
import com.amazon.ionschema.Violations
import com.amazon.ionschema.internal.util.IonSchema_2_0
import com.amazon.ionschema.internal.util.getFields
import com.amazon.ionschema.internal.util.getIslOptionalField
import com.amazon.ionschema.internal.util.getIslRequiredField
import com.amazon.ionschema.internal.util.islRequire
import com.amazon.ionschema.internal.util.islRequireElementType
import com.amazon.ionschema.internal.util.islRequireIonTypeNotNull
import com.amazon.ionschema.internal.util.islRequireOnlyExpectedFieldNames
import com.amazon.ionschema.internal.util.islRequireZeroOrOneElements
import com.amazon.ionschema.internal.util.markReadOnly
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Implementation of [Schema] for Ion Schema 2.0.
 */
internal class SchemaImpl_2_0 private constructor(
    referenceManager: DeferredReferenceManager,
    private val schemaSystem: IonSchemaSystemImpl,
    private val schemaCore: SchemaCore,
    schemaContent: Iterator<IonValue>,
    override val schemaId: String?,
    preloadedImports: Map<String, Import>,
    preloadedUserReservedFields: UserReservedFields,
    /*
     * [types] is declared as a MutableMap in order to be populated DURING
     * INITIALIZATION ONLY.  This enables type B to find its already-loaded
     * dependency type A.  After initialization, [types] is expected to
     * be treated as immutable as required by the Schema interface.
     */
    private val types: MutableMap<String, TypeInternal>,
) : SchemaInternal {

    /**
     * Represents the collection of symbols that are declared as user reserved fields for a schema.
     * See https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#open-content
     */
    private data class UserReservedFields(val headerWords: Set<String>, val typeWords: Set<String>, val footerWords: Set<String>) {
        companion object {
            @JvmStatic
            val NONE = UserReservedFields(emptySet(), emptySet(), emptySet())
        }
    }

    internal constructor(
        referenceManager: DeferredReferenceManager,
        schemaSystem: IonSchemaSystemImpl,
        schemaCore: SchemaCore,
        schemaContent: Iterator<IonValue>,
        schemaId: String?
    ) : this(referenceManager, schemaSystem, schemaCore, schemaContent, schemaId, emptyMap(), UserReservedFields.NONE, mutableMapOf())

    private val deferredTypeReferences = mutableListOf<TypeReferenceDeferred>()

    override val isl: IonDatagram

    private val imports: Map<String, Import>

    private var userReservedFields: UserReservedFields = UserReservedFields.NONE

    private val declaredTypes: Map<String, TypeImpl>

    override val ionSchemaLanguageVersion: IonSchemaVersion
        get() = IonSchemaVersion.v2_0

    init {
        val dgIsl = schemaSystem.ionSystem.newDatagram()

        if (types.isEmpty()) {

            var foundVersionMarker = false
            var foundHeader = false
            var foundFooter = false
            var foundAnyType = false
            var importsMap = emptyMap<String, Import>()

            while (schemaContent.hasNext()) {
                val it = schemaContent.next()

                dgIsl.add(it.clone())

                when {
                    IonSchemaVersion.isVersionMarker(it) -> {
                        islRequire(it.stringValue() == IonSchemaVersion.v2_0.symbolText) { "Unsupported Ion Schema version: $it" }
                        islRequire(!foundVersionMarker) { "Only one Ion Schema version marker is allowed in a schema document." }
                        islRequire(!foundHeader && !foundAnyType && !foundFooter) { "Ion Schema version marker must come before any header, types, and footer." }
                        foundVersionMarker = true
                    }
                    isHeader(it) -> {
                        if (!foundVersionMarker) throw IllegalStateException("SchemaImpl_2_0 should only be instantiated for an ISL 2.0 schema.")
                        islRequire(!foundAnyType) { "Schema header must appear before any types." }
                        islRequire(!foundHeader) { "Only one schema header is allowed in a schema document." }
                        importsMap = loadHeaderImports(types, it)
                        userReservedFields = loadUserReservedFieldNames(it)
                        validateFieldNamesInHeader(it)
                        foundHeader = true
                    }
                    isFooter(it) -> {
                        islRequire(foundHeader) { "Found a schema_footer, but no schema header precedes it." }
                        islRequire(!foundFooter) { "Only one schema footer is allowed in a schema document." }
                        validateFieldNamesInFooter(it)
                        foundFooter = true
                    }
                    isType(it) -> {
                        islRequire(!foundFooter) { "Types may not occur after the schema footer." }
                        it.getIslRequiredField<IonSymbol>("name")
                        val newType = TypeImpl(it, this, referenceManager)
                        islRequire(newType.name !in types.keys) { "Invalid duplicate type name: '${newType.name}'" }
                        addType(types, newType)
                        foundAnyType = true
                    }
                    isTopLevelOpenContent(it) -> {} // Fine; do nothing.
                    else -> {
                        throw InvalidSchemaException("Illegal top-level value in schema document: $it")
                    }
                }
            }
            islRequire(foundFooter || !foundHeader) { "Found a schema_header, but not a schema_footer" }

            resolveDeferredTypeReferences()
            imports = importsMap
        } else {
            // in this case the new Schema is based on an existing Schema and the 'types'
            // map was populated by the caller
            schemaContent.forEach {
                dgIsl.add(it.clone())
            }
            imports = preloadedImports
            userReservedFields = preloadedUserReservedFields
        }

        isl = dgIsl.markReadOnly()
        declaredTypes = types.values.filterIsInstance<TypeImpl>().associateBy { it.name }

        if (declaredTypes.isEmpty()) {
            schemaSystem.emitWarning { "${WarningType.SCHEMA_HAS_NO_TYPES} -- '$schemaId'" }
        }
    }

    private class SchemaAndTypeImports(val id: String, val schema: Schema) {
        var types: MutableMap<String, TypeInternal> = mutableMapOf()

        fun addType(name: String, type: TypeInternal) {
            types[name]?.let {
                if (it.schemaId != type.schemaId || it.isl != type.isl) {
                    throw InvalidSchemaException("Duplicate imported type name/alias encountered: '$name'")
                } else if (it is ImportedType && it.schemaId == it.importedFromSchemaId) {
                    return@addType
                }
            }
            types[name] = type
        }
    }

    /**
     * Checks whether a given value is allowed as top-level open content.
     * See https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#open-content
     */
    private fun isTopLevelOpenContent(value: IonValue): Boolean {
        if (IonSchemaVersion.isVersionMarker(value)) {
            return false
        }
        if (value.typeAnnotations.any { IonSchema_2_0.RESERVED_WORDS_REGEX.matches(it) }) {
            return false
        }
        return true
    }

    @OptIn(ExperimentalContracts::class)
    private fun isHeader(value: IonValue): Boolean {
        contract { returns(true) implies (value is IonStruct) }
        return value is IonStruct && !value.isNullValue && arrayOf("schema_header").contentDeepEquals(value.typeAnnotations)
    }

    @OptIn(ExperimentalContracts::class)
    private fun isFooter(value: IonValue): Boolean {
        contract { returns(true) implies (value is IonStruct) }
        return value is IonStruct && !value.isNullValue && arrayOf("schema_footer").contentDeepEquals(value.typeAnnotations)
    }

    @OptIn(ExperimentalContracts::class)
    private fun isType(value: IonValue): Boolean {
        contract { returns(true) implies (value is IonStruct) }
        return value is IonStruct && !value.isNullValue && arrayOf("type").contentDeepEquals(value.typeAnnotations)
    }

    /**
     * Constructs a [UserReservedFields] instance for the given Ion Schema header struct.
     */
    private fun loadUserReservedFieldNames(header: IonStruct): UserReservedFields {
        val userContent = header.getFields("user_reserved_fields")
            .islRequireZeroOrOneElements { "'user_reserved_fields' must only appear 0 or 1 times in the schema header" }

        userContent ?: return UserReservedFields.NONE

        islRequireIonTypeNotNull<IonStruct>(userContent) { "'user_reserved_fields' must be a non-null struct" }
        islRequire(userContent.typeAnnotations.isEmpty()) { "'user_reserved_fields' may not have any annotations" }
        return UserReservedFields(
            headerWords = loadUserReservedFieldsSubfield(userContent, "schema_header"),
            typeWords = loadUserReservedFieldsSubfield(userContent, "type"),
            footerWords = loadUserReservedFieldsSubfield(userContent, "schema_footer"),
        )
    }

    /**
     * Gets the list of field names that the user would like to reserve for a particular Ion Schema structure.
     */
    private fun loadUserReservedFieldsSubfield(userContent: IonStruct, fieldName: String): Set<String> {
        return userContent.getIslOptionalField<IonList>(fieldName)
            ?.islRequireElementType<IonSymbol>("list of user reserved symbols for $fieldName")
            ?.map { it.stringValue() }
            ?.onEach { islRequire(it !in IonSchema_2_0.KEYWORDS) { "Ion Schema 2.0 keyword '$it' may not be declared as a user reserved field: $userContent" } }
            ?.toSet()
            ?: emptySet()
    }

    private fun validateFieldNamesInHeader(header: IonStruct) {
        val unexpectedFieldNames = header.map { it.fieldName }
            .filterNot {
                it in IonSchema_2_0.HEADER_KEYWORDS ||
                    it in userReservedFields.headerWords ||
                    !IonSchema_2_0.RESERVED_WORDS_REGEX.matches(it)
            }
        islRequire(unexpectedFieldNames.isEmpty()) { "Found unexpected field names $unexpectedFieldNames in schema header: $header" }
    }

    private fun validateFieldNamesInFooter(footer: IonStruct) {
        val unexpectedFieldNames = footer.map { it.fieldName }
            .filterNot { it in userReservedFields.footerWords || !IonSchema_2_0.RESERVED_WORDS_REGEX.matches(it) }
        islRequire(unexpectedFieldNames.isEmpty()) { "Found unexpected field names $unexpectedFieldNames in schema footer: $footer" }
    }

    /**
     * Validates the fields names in a type definition. This function is `internal` so that it can be called for every
     * type definition that gets parsed.
     */
    internal fun validateFieldNamesInType(type: IonStruct) {
        val unexpectedFieldNames = type.map { it.fieldName }
            .filterNot { it in IonSchema_2_0.TYPE_KEYWORDS || it in userReservedFields.typeWords || !IonSchema_2_0.RESERVED_WORDS_REGEX.matches(it) }
        islRequire(unexpectedFieldNames.isEmpty()) { "Found unexpected field names $unexpectedFieldNames in type definition: $type" }
    }

    private fun loadHeaderImports(
        typeMap: MutableMap<String, TypeInternal>,
        header: IonStruct
    ): Map<String, Import> {

        val importsMap = mutableMapOf<String, SchemaAndTypeImports>()
        val importSet: MutableSet<String> = schemaSystem.getSchemaImportSet()

        val imports = header.getIslOptionalField<IonList>("imports")
            ?.islRequireElementType<IonStruct>(containerDescription = "imports list")
            // If there's no imports field, then there's nothing to do
            ?: return emptyMap()

        imports.forEach {
            it.islRequireOnlyExpectedFieldNames(IonSchema_2_0.IMPORT_KEYWORDS)

            val idField = it.getIslRequiredField<IonText>("id")
            val typeField = it.getIslOptionalField<IonSymbol>("type")
            val asField = it.getIslOptionalField<IonSymbol>("as")

            typeField ?: islRequire(asField == null) { "'as' only allowed when 'type' is present: $it" }

            val importedSchemaId = idField.stringValue()
            // if Schema is importing itself then throw error
            if (schemaId == importedSchemaId) {
                throw InvalidSchemaException("Schema can not import itself: $it")
            }
            // if importSet has an import with this id then do not load schema again to break the cycle.
            if (!importSet.contains(importedSchemaId)) {

                // add current schema to importSet and continue loading current schema
                importSet.add(importedSchemaId)
                val importedSchema = runCatching { schemaSystem.loadSchema(importedSchemaId) }
                    .getOrElse { e -> throw InvalidSchemaException("Unable to load schema '$importedSchemaId'; ${e.message}") }
                importSet.remove(importedSchemaId)

                val schemaAndTypes = importsMap.getOrPut(importedSchemaId) {
                    SchemaAndTypeImports(importedSchemaId, importedSchema)
                }

                val typeName = typeField?.stringValue()
                if (typeName != null) {
                    var importedType = importedSchema.getDeclaredType(typeName)
                        ?.toImportedType(importedSchemaId)

                    importedType ?: throw InvalidSchemaException("Schema $importedSchemaId doesn't contain a type named '$typeName'")

                    if (asField != null) {
                        importedType = TypeAliased(asField, importedType)
                    }
                    addType(typeMap, importedType)
                    schemaAndTypes.addType(asField?.stringValue() ?: typeName, importedType)
                } else {
                    val typesToAdd = importedSchema.getDeclaredTypes()

                    typesToAdd.asSequence()
                        .map { type -> type.toImportedType(importedSchemaId) }
                        .forEach { type ->
                            addType(typeMap, type)
                            schemaAndTypes.addType(type.name, type)
                        }
                }
            }
        }
        return importsMap.mapValues {
            ImportImpl(it.value.id, it.value.schema, it.value.types)
        }
    }

    override fun getImport(id: String) = imports[id]

    override fun getImports() = imports.values.iterator()

    private fun validateType(type: Type) {
        val struct = type.isl as IonStruct
        val name = struct["name"]
        if (name == null || name.isNullValue) {
            throw InvalidSchemaException(
                "Top-level types of a schema must have a name ($type.isl)"
            )
        }
    }

    private fun addType(typeMap: MutableMap<String, TypeInternal>, type: TypeInternal) {
        validateType(type)
        getType(type.name)?.let {
            if (it.schemaId != type.schemaId || it.isl != type.isl) {
                throw InvalidSchemaException("Duplicate type name/alias encountered: '${it.name}'")
            }
        }
        typeMap[type.name] = type
    }

    override fun getInScopeType(name: String) = getType(name)

    override fun getType(name: String) = schemaCore.getType(name) ?: types[name]

    override fun getDeclaredType(name: String) = declaredTypes[name]

    override fun getDeclaredTypes(): Iterator<TypeInternal> = declaredTypes.values.iterator()

    override fun getTypes(): Iterator<TypeInternal> =
        (schemaCore.getTypes().asSequence() + types.values.asSequence())
            .filter { it is ImportedType || it is TypeImpl }
            .iterator()

    override fun newType(isl: String) = newType(
        schemaSystem.ionSystem.singleValue(isl) as IonStruct
    )

    override fun newType(isl: IonStruct): Type {
        return schemaSystem.usingReferenceManager { TypeImpl(isl, this, it) }
            .also { resolveDeferredTypeReferences() }
    }

    override fun plusType(type: Type): Schema {
        type as TypeInternal
        validateType(type)

        // prepare ISL corresponding to the new Schema
        // (might be simpler if IonDatagram.set(int, IonValue) were implemented,
        // see https://github.com/amazon-ion/ion-java/issues/50)
        val newIsl = schemaSystem.ionSystem.newDatagram()
        var newTypeAdded = false
        isl.forEachIndexed { idx, value ->
            if (!newTypeAdded) {
                when {
                    value is IonStruct
                        && (value["name"] as? IonSymbol)?.stringValue().equals(type.name) -> {
                        // new type replaces existing type of the same name
                        newIsl.add(type.isl.clone())
                        newTypeAdded = true
                        return@forEachIndexed
                    }
                    (value is IonStruct && value.hasTypeAnnotation("schema_footer"))
                        || idx == isl.lastIndex -> {
                        newIsl.add(type.isl.clone())
                        newTypeAdded = true
                    }
                }
            }
            newIsl.add(value.clone())
        }
        if (!newTypeAdded) newIsl.add(type.isl.clone())

        // clone the types map:
        val preLoadedTypes = types.toMutableMap()
        preLoadedTypes[type.name] = type
        return schemaSystem.usingReferenceManager { referenceManager ->
            SchemaImpl_2_0(referenceManager, schemaSystem, schemaCore, newIsl.iterator(), null, imports, userReservedFields, preLoadedTypes)
        }
    }

    override fun getSchemaSystem() = schemaSystem

    override fun addDeferredType(typeRef: TypeReferenceDeferred) {
        deferredTypeReferences.add(typeRef)
    }

    private fun resolveDeferredTypeReferences() {
        val unresolvedDeferredTypeReferences = deferredTypeReferences
            .filterNot { it.attemptToResolve() }
            .map { it.name }.toSet()

        if (unresolvedDeferredTypeReferences.isNotEmpty()) {
            throw InvalidSchemaException(
                "Unable to resolve type reference(s): $unresolvedDeferredTypeReferences"
            )
        }
    }

    /**
     * Returns a new [ImportedType] instance that decorates [Type] so that it will
     * log a transitive import warning every time it is used for validation.
     */
    private fun Type.toImportedType(importedFromSchemaId: String): ImportedType {
        this@toImportedType as TypeInternal
        return object : ImportedType, TypeInternal by this {
            override fun validate(value: IonValue, issues: Violations) {
                if (importedFromSchemaId != schemaId) {
                    schemaSystem.emitWarning {
                        warnInvalidTransitiveImport(this, this@SchemaImpl_2_0.schemaId)
                    }
                }
                this@toImportedType.validate(value, issues)
            }

            override val schemaId: String
                get() = this@toImportedType.schemaId!!
            override val importedFromSchemaId: String
                get() = importedFromSchemaId
        }
    }
}
