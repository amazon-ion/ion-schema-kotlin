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

package com.amazon.ionschema

import com.amazon.ion.IonDatagram
import com.amazon.ion.IonStruct
import com.amazon.ion.IonValue
import com.amazon.ionschema.IonSchemaTestFilesSource.Companion.ION_SCHEMA_TESTS_DIR
import com.amazon.ionschema.internal.IonSchemaSystemImpl
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File
import java.io.FileReader
import kotlin.test.assertEquals

/**
 * This test runner uses the ISL for ISL itself to validate the types and schemas
 * defined in the file-based test suite.
 */
class ISLforISLTestRunner {

    companion object {
        const val ION_SCHEMA_SCHEMAS_DIR = "../ion-schema-schemas"

        // There are certain conditions that make a schema invalid that cannot
        // be detected by the ISL for ISL (for example, a schema importing itself).
        // Test files with "invalid_schema" definitions should be listed here if and
        // only if they are invalid for some reason that cannot be validated by ISL.
        val EXCLUDE_LIST = listOf(
            "../ion-schema-tests/schema/import/import_type_unknown.isl",
            "../ion-schema-tests/schema/import/invalid_duplicate_import.isl",
            "../ion-schema-tests/schema/import/invalid_duplicate_import_type.isl",
            "../ion-schema-tests/schema/import/invalid_duplicate_type.isl",
            "../ion-schema-tests/schema/import/invalid_transitive_import_of_schema.isl",
            "../ion-schema-tests/schema/import/invalid_transitive_import_of_type.isl",
            "../ion-schema-tests/schema/import/invalid_transitive_import_of_type_by_alias.isl",
            "../ion-schema-tests/schema/invalid_missing_schema_footer.isl",
            "../ion-schema-tests/schema/invalid_missing_schema_header.isl",
            "../ion-schema-tests/schema/invalid_reuse_of_type_name.isl",
            "../ion-schema-tests/schema/invalid_unknown_type.isl"
        )
    }

    private val schemaSystem = IonSchemaSystemBuilder.standard()
        .withAuthority(AuthorityFilesystem(ION_SCHEMA_SCHEMAS_DIR))
        .allowTransitiveImports(false)
        .build() as IonSchemaSystemImpl

    @TestFactory
    fun generateIslForIslTestSuite(): Iterable<DynamicNode> {
        return IonSchemaTestFilesSource.asSequence(excluding = EXCLUDE_LIST)
            .map { generateTestCasesForFile(it) }
            .asIterable()
    }

    private fun generateTestCasesForFile(file: File): DynamicNode {
        val islSchema = schemaSystem.loadSchema("isl/schema.isl")
        val schema = islSchema.getType("schema")!!
        val type = islSchema.getType("type")!!

        val testName = file.relativeTo(File(ION_SCHEMA_TESTS_DIR)).path.dropLast(4) // drop ".isl"
        val testFileIon = ION.iterate(FileReader(file)).asSequence().toList()

        return dynamicContainer(
            testName,
            testFileIon.mapIndexedNotNull { index, ion ->
                when (ion.typeAnnotations.getOrNull(0)) {
                    "schema_header" -> createIslTestCase(schema, extractSchemaDatagram(testFileIon, index), expectValid = true)
                    "invalid_schema" -> createIslTestCase(schema, prepareValue(ion), expectValid = false)
                    "type" -> createIslTestCase(type, ensureTypeHasName(ion), expectValid = true)
                    "invalid_type" -> createIslTestCase(type, ion, expectValid = false)
                    else -> null
                }
            }
        )
    }

    private fun ensureTypeHasName(ion: IonValue): IonStruct {
        return (ion as IonStruct).also {
            it["name"] ?: it.put("name", ION.newSymbol("placeholder_name"))
        }
    }

    private fun extractSchemaDatagram(testFileIon: List<IonValue>, headerPosition: Int): IonDatagram {
        var toIndex = testFileIon.size
        for (i in headerPosition until testFileIon.size) {
            if (testFileIon[i].hasTypeAnnotation("schema_footer")) {
                toIndex = i + 1
                break
            }
        }
        return ION.newDatagram().apply {
            addAll(testFileIon.subList(headerPosition, toIndex))
        }
    }

    private fun createIslTestCase(type: Type, isl: IonValue, expectValid: Boolean): DynamicNode {
        val testName = "Should${if (expectValid) " " else " not "}be a valid ${type.name}: $isl"
        return dynamicTest(testName) {
            println(isl)
            val result = type.validate(isl)
            println(result)
            assertEquals(expectValid, result.isValid())
        }
    }
}
