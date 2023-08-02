package com.amazon.ionschema.util

import com.amazon.ionschema.IonSchemaSystemBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SchemaSymbolsUtilTest {

    companion object {
        @JvmField
        val ION_SCHEMA_2_0_SYMBOLS = setOf(
            // Version marker
            "\$ion_schema_2_0",
            // Built in types
            "any", "\$any",
            "blob", "\$blob",
            "bool", "\$bool",
            "clob", "\$clob",
            "decimal", "\$decimal",
            "document",
            "float", "\$float",
            "int", "\$int",
            "list", "\$list",
            "lob", "\$lob",
            "nothing",
            "\$null",
            "number", "\$number",
            "sexp", "\$sexp",
            "string", "\$string",
            "struct", "\$struct",
            "symbol", "\$symbol",
            "text", "\$text",
            "timestamp", "\$timestamp",
            // Keywords
            "all_of",
            "annotations",
            "any_of",
            "as",
            "byte_length",
            "codepoint_length",
            "container_length",
            "contains",
            "element",
            "exponent",
            "field_names",
            "fields",
            "id",
            "ieee754_float",
            "imports",
            "name",
            "not",
            "occurs",
            "one_of",
            "ordered_elements",
            "precision",
            "regex",
            "schema_footer",
            "schema_header",
            "timestamp_offset",
            "timestamp_precision",
            "type",
            "user_reserved_fields",
            "utf8_byte_length",
            "valid_values",
            // Ranges
            "range",
            "min",
            "max",
            "exclusive",
            "optional",
            "required",
            // Timestamp Precisions
            "year",
            "month",
            "day",
            "minute",
            "second",
            "millisecond",
            "microsecond",
            "nanosecond",
            // Float types
            "binary16",
            "binary32",
            "binary64",
            // Misc modifier annotations
            "i",
            "m",
            "closed",
            "distinct",
            "required",
            "\$null_or",
        )
    }

    @Test
    fun `Ion Schema 2 0 should have expected symbols`() {
        val symbols = SchemaSymbolsUtil.getSymbolsTextForPath("./src/main/resources/ion-schema-schemas/") { it.path.contains("isl/ion_schema_2_0") }
        // Sorted so that it's easier to see the diff when this test fails
        assertEquals(ION_SCHEMA_2_0_SYMBOLS.sorted(), symbols.sorted())
    }

    @Test
    fun `getSymbolsTextForSchema should get all expected symbols in a schema`() {
        val schema = """
            ${'$'}ion_schema_2_0
            type::{
              name: foo_type,
              annotations: closed::[foo],
              fields: {
                a1: int,
                a2: { valid_values: [1, true, yes] },
                a3: { type: symbol, codepoint_length: range::[1, 10] },
                haystack: { type: list, contains: [needle] },
              }
            }
            
            type::{
              name: bar_type,
              type: {
                one_of: [
                  { 
                    any_of: [
                      { valid_values: [a, b, c] },
                      { annotations: required::[d, e, f] },
                    ]
                  },
                  { 
                    all_of: [
                      { contains: [g, h, i] },
                      { not: { fields: { j: nothing } } }
                    ] 
                  }
                ]
              }
            }
        """.trimIndent()
        val expected = setOf(
            "foo", "a1", "a2", "a3", "yes", "haystack", "needle",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
        )
        val iss = IonSchemaSystemBuilder.standard().build()

        val symbols = SchemaSymbolsUtil.getSymbolsTextForSchema(iss.newSchema(schema))
        // Sorted so that it's easier to see the diff when this test fails
        assertEquals(expected.sorted(), symbols.sorted())
    }
}
