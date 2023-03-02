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
internal class SchemaImpl_2_0 internal constructor(
    referenceManager: DeferredReferenceManager,
    private val schemaSystem: IonSchemaSystemImpl,
    schemaContent: Iterable<IonValue>,
    override val schemaId: String?,

) : SchemaInternal {

    override val isl: IonDatagram
    override val ionSchemaLanguageVersion = IonSchemaVersion.v2_0

    /**
     * [importedTypes] is declared as a MutableMap in order to be populated DURING
     * INITIALIZATION ONLY.  This enables type B to find its already-loaded
     * dependency type A.  After initialization, [importedTypes] is expected to
     * be treated as immutable as required by the Schema interface.
     */
    private val importedTypes: MutableMap<String, TypeInternal> = mutableMapOf()
    private val declaredTypes: MutableMap<String, TypeImpl> = mutableMapOf()
    private var userReservedFields: UserReservedFields = UserReservedFields.NONE
    private var imports: Map<String, Import> = emptyMap()

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

    private val deferredTypeReferences = mutableListOf<TypeReferenceDeferred>()

    init {
        if (schemaId != null) referenceManager.registerDependentSchema(schemaId)
        isl = schemaSystem.ionSystem.newDatagram()

        var foundVersionMarker = false
        var foundHeader = false
        var foundFooter = false
        var foundAnyType = false

        importedTypes += schemaSystem.getBuiltInTypesSchema(ionSchemaLanguageVersion)
            .getDeclaredTypes()
            .asSequence()
            .associateBy { it.name }

        schemaContent.mapTo(isl) { it.clone() }
            .markReadOnly()
            .forEach {
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
                        imports = loadHeaderImports(it, referenceManager)
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
                        addType(declaredTypes, TypeImpl(it, this, referenceManager))
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
        header: IonStruct,
        referenceManager: DeferredReferenceManager,
    ): Map<String, Import> {

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
            islRequire(schemaId != importedSchemaId) { "Schema can not import itself: $it" }
            islRequire(schemaSystem.doesSchemaDocumentExist(importedSchemaId)) { "No such schema: $importedSchemaId" }

            when {
                asField != null -> {
                    if (schemaSystem.doesSchemaDeclareType(importedSchemaId, typeField!!)) {
                        val deferred = referenceManager.createDeferredImportReference(importedSchemaId, typeField)
                        val type = TypeAliased(asField, deferred)
                        addType(importedTypes, type)
                    } else {
                        throw InvalidSchemaException("No such type $typeField in schema $importedSchemaId")
                    }
                }
                typeField != null -> {
                    if (schemaSystem.doesSchemaDeclareType(importedSchemaId, typeField)) {
                        val deferred = referenceManager.createDeferredImportReference(importedSchemaId, typeField)
                        addType(importedTypes, deferred)
                    } else {
                        throw InvalidSchemaException("No such type $typeField in schema $importedSchemaId")
                    }
                }
                else -> {
                    schemaSystem.listDeclaredTypes(importedSchemaId).forEach { importedTypeName ->
                        val deferred = referenceManager.createDeferredImportReference(importedSchemaId, importedTypeName)
                        addType(importedTypes, deferred)
                    }
                }
            }
        }
        return importedTypes.values
            .filterIsInstance<ImportedType>()
            .groupBy { it.schemaId }
            .mapValues { (id, importedTypes) ->
                ImportImpl(id, { schemaSystem.loadSchema(id) }, importedTypes.associateBy { it.name })
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

    private fun <T : Type> addType(types: MutableMap<String, T>, type: T) {
        // If it's not a struct, then it's an imported type, and we only have a name symbol
        if (type.isl is IonStruct) validateType(type)

        getType(type.name)?.let {
            if (type !is ImportedType || it !is ImportedType) {
                throw InvalidSchemaException("Duplicate type name/alias encountered: '${it.name}'")
            }

            if (it.schemaId != type.schemaId || it.isl != type.isl) {
                throw InvalidSchemaException("Duplicate type name/alias encountered: '${it.name}'")
            } else if (it.importedFromSchemaId != type.importedFromSchemaId) {
                throw InvalidSchemaException("Duplicate type name/alias encountered: '${it.name}'")
            }
        }
        types[type.name] = type
    }

    override fun getInScopeType(name: String) = getType(name)

    override fun getType(name: String) = importedTypes[name] ?: declaredTypes[name]

    override fun getDeclaredType(name: String) = declaredTypes[name]

    override fun getDeclaredTypes(): Iterator<TypeInternal> = declaredTypes.values.iterator()

    override fun getTypes(): Iterator<TypeInternal> = (declaredTypes.values + importedTypes.values.filterIsInstance<TypeBuiltin>()).iterator()

    override fun newType(isl: String) = newType(
        schemaSystem.ionSystem.singleValue(isl) as IonStruct
    )

    override fun newType(isl: IonStruct): Type {
        return schemaSystem.usingReferenceManager { TypeImpl(isl, this, it) }
            .also { resolveDeferredTypeReferences() }
    }

    override fun plusType(type: Type): Schema {
        validateType(type)

        val newIsl = mutableListOf<IonValue>()
        var newTypeAdded = false
        isl.forEachIndexed { idx, value ->
            if (!newTypeAdded) {
                if (isType(value) && (value["name"] as? IonSymbol)?.stringValue() == type.name) {
                    // new type replaces existing type of the same name
                    newIsl.add(type.isl.clone())
                    newTypeAdded = true
                    return@forEachIndexed
                } else if (value.hasTypeAnnotation("schema_footer") || idx == isl.lastIndex) {
                    newIsl.add(type.isl.clone())
                    newTypeAdded = true
                }
            }
            newIsl.add(value.clone())
        }
        if (!newTypeAdded) newIsl.add(type.isl.clone())

        return schemaSystem.newSchema(newIsl.iterator())
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
}
