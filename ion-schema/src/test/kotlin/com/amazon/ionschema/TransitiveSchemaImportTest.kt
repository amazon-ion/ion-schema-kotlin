package com.amazon.ionschema

import com.amazon.ionschema.TransitiveSchemaImportTest.SchemaAssertion.DoesNotHaveType
import com.amazon.ionschema.TransitiveSchemaImportTest.SchemaAssertion.HasType
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * This tests that the old, incorrect behavior mentioned in
 * [amazon-ion/ion-schema-kotlin#178](https://github.com/amazon-ion/ion-schema-kotlin/issues/178)
 * still works as before even after implementing the bugfix.
 */
class TransitiveSchemaImportTest {

    private val TEST_SCHEMAS = mapOf(
        "numbers" to """
            type::{
              name: positive_int,
              type: int,
              valid_values: range::[1, max]
            }
            type::{
              name: negative_int,
              type: int,
              valid_values: range::[min, -1]
            }
        """,
        "import_schema" to """
            schema_header::{
              imports: [
                { id: numbers },
              ],
            }
            schema_footer::{}
        """,
        "transitive_import_schema" to """
            schema_header::{
              imports: [
                { id: import_schema },
              ],
            }
            schema_footer::{}
        """,
        "inline_transitive_import_schema" to """
            type::{
              name: foo,
              element: { id: transitive_import_schema, type: positive_int }
            }
        """,
        "import_type" to """
            schema_header::{
              imports: [
                { id: numbers, type: positive_int },
              ],
            }
            schema_footer::{}
        """,
        "transitive_import_type" to """
            schema_header::{
              imports: [
                { id: import_schema, type: positive_int },
              ],
            }
            schema_footer::{}
        """,
        "inline_transitive_import_type" to """
            type::{
              name: foo,
              element: { id: transitive_import_schema, type: positive_int }
            }
        """,
        "import_type_with_alias" to """
            schema_header::{
              imports: [
                { id: numbers, type: positive_int, as: positive_int_1 },
              ],
            }
            schema_footer::{}
        """,
        "transitive_import_type_with_alias" to """
            schema_header::{
              imports: [
                { id: import_type_with_alias },
              ],
            }
            schema_footer::{}
        """,
        "inline_transitive_import_type_with_alias" to """
            type::{
              name: foo,
              element: { id: import_type_with_alias, type: positive_int_1 }
            }
        """
    )

    sealed class SchemaAssertion {
        data class HasType(val typeName: String) : SchemaAssertion()
        data class DoesNotHaveType(val typeName: String) : SchemaAssertion()
    }

    val iss = IonSchemaSystemBuilder.standard()
        .allowTransitiveImports(true)
        .withAuthority(InMemoryMapAuthority.fromIonText(TEST_SCHEMAS))
        .withWarningMessageCallback { println(it) }
        .build()

    companion object {
        @JvmStatic
        fun testCaseData() = arrayOf(
            arrayOf("import_schema", HasType("positive_int")),
            arrayOf("import_schema", HasType("negative_int")),
            arrayOf("transitive_import_schema", HasType("positive_int")),
            arrayOf("transitive_import_schema", HasType("negative_int")),
            arrayOf("inline_transitive_import_schema", HasType("foo")),
            arrayOf("inline_transitive_import_schema", DoesNotHaveType("positive_int")),
            arrayOf("inline_transitive_import_schema", DoesNotHaveType("negative_int")),
            arrayOf("import_type", HasType("positive_int")),
            arrayOf("import_type", DoesNotHaveType("negative_int")),
            arrayOf("transitive_import_type", HasType("positive_int")),
            arrayOf("transitive_import_type", DoesNotHaveType("negative_int")),
            arrayOf("inline_transitive_import_type", HasType("foo")),
            arrayOf("inline_transitive_import_type", DoesNotHaveType("positive_int")),
            arrayOf("inline_transitive_import_type", DoesNotHaveType("negative_int")),
            arrayOf("import_type_with_alias", HasType("positive_int_1")),
            arrayOf("import_type_with_alias", DoesNotHaveType("positive_int")),
            arrayOf("import_type_with_alias", DoesNotHaveType("negative_int")),
            arrayOf("transitive_import_type_with_alias", HasType("positive_int_1")),
            arrayOf("transitive_import_type_with_alias", DoesNotHaveType("positive_int")),
            arrayOf("transitive_import_type_with_alias", DoesNotHaveType("negative_int")),
            arrayOf("inline_transitive_import_type_with_alias", HasType("foo")),
            arrayOf("inline_transitive_import_type_with_alias", DoesNotHaveType("positive_int")),
            arrayOf("inline_transitive_import_type_with_alias", DoesNotHaveType("positive_int_1")),
            arrayOf("inline_transitive_import_type_with_alias", DoesNotHaveType("negative_int"))
        )
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("testCaseData")
    fun testSchemaImports(schemaId: String, schemaAssertion: SchemaAssertion) {
        val schema = iss.loadSchema(schemaId)
        when (schemaAssertion) {
            is HasType -> assertNotNull(schema.getType(schemaAssertion.typeName))
            is DoesNotHaveType -> assertNull(schema.getType(schemaAssertion.typeName))
        }
    }
}

class TransitiveSchemaImportLogWarningTest {

    private val ABC_SCHEMAS = mapOf(
        "schema_A" to """
            type::{
              name: type_A,
              element: int,
            }
        """,
        "schema_B" to """
            schema_header::{
              imports: [
                { id: schema_A }
              ]
            }
            type::{
              name: type_B,
              element: type_A,
            }
            schema_footer::{}
        """,
        "schema_C" to """
            schema_header::{
              imports: [
                { id: schema_B }
              ]
            }
            type::{
              name: type_C,
              element: type_B,
            }
            schema_footer::{}
        """
    )

    private val validateAValue: Schema.() -> Unit = {
        getType("the_type")!!.validate(getSchemaSystem().ionSystem.singleValue("irrelevant"))
    }

    private fun collectLogLines(schemaString: String, action: Schema.() -> Unit = validateAValue): List<String> {
        val logLines = mutableListOf<String>()
        IonSchemaSystemBuilder.standard()
            .withWarningMessageCallback { logLines.add(it) }
            .withAuthority(InMemoryMapAuthority.fromIonText(ABC_SCHEMAS + ("test_case_schema" to schemaString)))
            .build()
            .loadSchema("test_case_schema")
            .run(action)
        return logLines.toList()
    }

    private fun assertLogsMatchPrefixes(expectedPrefixes: List<String>, logLines: List<String>) {
        assertTrue(
            message = "Log lines do not match expected prefixes; expected:" +
                "<${expectedPrefixes.joinToString("\n") { "$it..." } }> " +
                "but was:<${logLines.joinToString("\n")}>"
        ) {
            logLines
                .mapIndexed { index, s ->
                    expectedPrefixes.getOrNull(index)
                        ?.let { s.startsWith(it) }
                        ?: false
                }
                .all { it } // All true
        }
        assertEquals(logLines.size, expectedPrefixes.size)
    }

    @Test
    fun whenASchemaUsesATransitivelyImportedType_aWarningShouldBeLogged() {
        val schema = """
            schema_header::{
              imports: [
                { id: schema_B },
              ]
            }
            type::{
              name: the_type,
              not: type_A,
            }
            schema_footer::{}
        """

        val logLines = collectLogLines(schema, action = validateAValue)

        val expectedPrefixes = listOf("INVALID_TRANSITIVE_IMPORT")
        assertLogsMatchPrefixes(expectedPrefixes, logLines)
    }

    @Test
    fun whenASchemaDeclaresNoTypes_aWarningShouldBeLogged() {
        val schema = """
            schema_header::{
              imports: [
                { id: schema_B },
              ]
            }
            schema_footer::{}
        """

        val logLines = collectLogLines(schema, action = {})

        val expectedPrefixes = listOf("SCHEMA_HAS_NO_TYPES")
        assertLogsMatchPrefixes(expectedPrefixes, logLines)
    }

    @Test
    fun whenThereIsBothTransitiveAndNonTransitiveImportPathsToSameType_noWarningShouldBeLogged_case1() {
        val schema = """
            schema_header::{
              imports: [
                { id: schema_A },
                { id: schema_B },
              ]
            }
            type::{
              name: the_type,
              not: type_A,
            }
            schema_footer::{}
        """

        val logLines = collectLogLines(schema, action = validateAValue)
        assertEquals(emptyList(), logLines)
    }

    @Test
    fun whenThereIsBothTransitiveAndNonTransitiveImportPathsToSameType_noWarningShouldBeLogged_case2() {
        val schema = """
            schema_header::{
              imports: [
                // The order here is flipped compared to the previous test case
                // to demonstrate that the order of the imports should not matter
                { id: schema_B },
                { id: schema_A },
              ]
            }
            type::{
              name: the_type,
              not: type_A,
            }
            schema_footer::{}
        """

        val logLines = collectLogLines(schema, action = validateAValue)
        assertEquals(emptyList(), logLines)
    }

    @Test
    fun whenThereIsATransitiveImportOfATypeByName_regardlessOfAnyOtherImportPaths_aWarningShouldBeLogged() {
        val schema = """
            schema_header::{
              imports: [
                { id: schema_A },
                { id: schema_B, type: type_A }, // This should cause a warning in spite of the prior line
              ]
            }
            type::{
              name: the_type,
              not: type_A,
            }
            schema_footer::{}
        """

        val logLines = collectLogLines(schema, action = validateAValue)

        val expectedPrefixes = listOf("INVALID_TRANSITIVE_IMPORT")
        assertLogsMatchPrefixes(expectedPrefixes, logLines)
    }

    @Test
    fun whenATransitivelyImportedTypeIsGivenAnAlias_aWarningShouldBeLogged() {
        val schema = """
            schema_header::{
              imports: [
                { id: schema_A },
                { id: schema_B, type: type_A, as: not_type_A },
              ]
            }
            type::{
              name: the_type,
              not: not_type_A,
            }
            schema_footer::{}
        """

        val logLines = collectLogLines(schema, action = validateAValue)

        val expectedPrefixes = listOf(
            "INVALID_TRANSITIVE_IMPORT",
            "INVALID_TRANSITIVE_IMPORT"
        )
        assertLogsMatchPrefixes(expectedPrefixes, logLines)
    }
}
