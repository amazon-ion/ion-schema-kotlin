package com.amazon.ionschema.reader.internal

import com.amazon.ion.IonStruct
import com.amazon.ion.IonValue
import com.amazon.ionschema.internal.util.islRequire
import com.amazon.ionschema.internal.util.islRequireExactAnnotations
import com.amazon.ionschema.internal.util.islRequireIonTypeNotNull
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.SchemaDocument
import com.amazon.ionschema.util.toBag

@ExperimentalIonSchemaModel
internal class FooterReader(private val isValidOpenContentField: ReaderContext.(String) -> Boolean) {

    fun readFooter(context: ReaderContext, footerValue: IonValue): SchemaDocument.Item.Footer {
        islRequireIonTypeNotNull<IonStruct>(footerValue) { "schema_footer must be a non-null struct; was: $footerValue" }
        islRequireExactAnnotations(footerValue, "schema_footer") { "schema_footer may not have extra annotations" }

        val unexpectedFieldNames = footerValue.map { it.fieldName }.filterNot { context.isValidOpenContentField(it) }

        islRequire(unexpectedFieldNames.isEmpty()) { "Found illegal field names $unexpectedFieldNames in schema footer: $footerValue" }

        val openContent = footerValue.map { it.fieldName to it }.toBag()
        return SchemaDocument.Item.Footer(openContent)
    }
}
