package com.amazon.ionschema.reader.internal

import com.amazon.ion.IonList
import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonText
import com.amazon.ion.IonValue
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.internal.util.IonSchema_2_0
import com.amazon.ionschema.internal.util.getFields
import com.amazon.ionschema.internal.util.getIslOptionalField
import com.amazon.ionschema.internal.util.getIslRequiredField
import com.amazon.ionschema.internal.util.islRequire
import com.amazon.ionschema.internal.util.islRequireElementType
import com.amazon.ionschema.internal.util.islRequireExactAnnotations
import com.amazon.ionschema.internal.util.islRequireIonTypeNotNull
import com.amazon.ionschema.internal.util.islRequireNoIllegalAnnotations
import com.amazon.ionschema.internal.util.islRequireOnlyExpectedFieldNames
import com.amazon.ionschema.internal.util.islRequireZeroOrOneElements
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.HeaderImport
import com.amazon.ionschema.model.SchemaHeader
import com.amazon.ionschema.model.UserReservedFields
import com.amazon.ionschema.util.toBag

@ExperimentalIonSchemaModel
internal class HeaderReader(private val ionSchemaVersion: IonSchemaVersion) {

    /**
     * Reads the header
     */
    fun readHeader(context: ReaderContext, headerValue: IonValue): SchemaHeader {
        islRequire(!context.foundHeader) { "Only one schema header is allowed in a schema document." }
        islRequire(!context.foundAnyType) { "Schema header must appear before any types." }
        context.foundHeader = true

        islRequireIonTypeNotNull<IonStruct>(headerValue) { "schema_header must be a non-null struct; was: $headerValue" }
        islRequireExactAnnotations(headerValue, "schema_header") { "schema_header may not have extra annotations" }
        val imports = loadHeaderImports(context, headerValue)

        if (ionSchemaVersion != IonSchemaVersion.v1_0) {
            context.userReservedFields = loadUserReservedFieldNames(headerValue)
            validateFieldNamesInHeader(context, headerValue)
        }

        val headerKeywords = when (ionSchemaVersion) {
            IonSchemaVersion.v1_0 -> setOf("imports")
            IonSchemaVersion.v2_0 -> IonSchema_2_0.HEADER_KEYWORDS
        }

        val openContent = headerValue.filter { it.fieldName !in headerKeywords }
            .map { it.fieldName to it }
            .toBag()

        return SchemaHeader(imports, context.userReservedFields, openContent)
    }

    /**
     * Constructs a [UserReservedFields] instance for the given Ion Schema header struct.
     */
    private fun loadUserReservedFieldNames(header: IonStruct): UserReservedFields {
        val userContent = header.getFields("user_reserved_fields")
            .islRequireZeroOrOneElements { "'user_reserved_fields' must only appear 0 or 1 times in the schema header" }

        userContent ?: return UserReservedFields()

        islRequireIonTypeNotNull<IonStruct>(userContent) { "'user_reserved_fields' must be a non-null struct" }
        userContent.islRequireOnlyExpectedFieldNames(IonSchema_2_0.TOP_LEVEL_ANNOTATION_KEYWORDS)
        islRequire(userContent.typeAnnotations.isEmpty()) { "'user_reserved_fields' may not have any annotations" }
        return UserReservedFields(
            header = loadUserReservedFieldsSubfield(userContent, "schema_header"),
            type = loadUserReservedFieldsSubfield(userContent, "type"),
            footer = loadUserReservedFieldsSubfield(userContent, "schema_footer")
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

    private fun loadHeaderImports(context: ReaderContext, header: IonStruct): Set<HeaderImport> {
        // If there's no imports field, then there's nothing to do
        val imports = header.getIslOptionalField<IonList>("imports") ?: return emptySet()

        islRequireNoIllegalAnnotations(imports) { "'imports' list may not be annotated" }

        return imports.readAllCatching(context) {
            islRequireIonTypeNotNull<IonStruct>(it) { "header import must be a non-null struct; was: $this" }
            it.islRequireOnlyExpectedFieldNames(IonSchema_2_0.IMPORT_KEYWORDS)
            islRequireNoIllegalAnnotations(it) { "import struct may not have any annotations" }

            val schemaId = it.getIslRequiredField<IonText>("id").stringValue()
            val typeField = it.getIslOptionalField<IonSymbol>("type")
            val asField = it.getIslOptionalField<IonSymbol>("as")

            when {
                asField != null -> {
                    islRequire(typeField != null) { "'as' only allowed when 'type' is present: $it" }
                    HeaderImport.Type(schemaId, typeField.stringValue(), asField.stringValue())
                }
                typeField != null -> HeaderImport.Type(schemaId, typeField.stringValue())
                else -> HeaderImport.Wildcard(schemaId)
            }
        }.toSet()
    }

    private fun validateFieldNamesInHeader(context: ReaderContext, header: IonStruct) {
        val unexpectedFieldNames = header.map { it.fieldName }
            .filterNot {
                it in IonSchema_2_0.HEADER_KEYWORDS ||
                    it in context.userReservedFields.header ||
                    !IonSchema_2_0.RESERVED_WORDS_REGEX.matches(it)
            }
        islRequire(unexpectedFieldNames.isEmpty()) { "Found unexpected field names $unexpectedFieldNames in schema header: $header" }
    }
}
