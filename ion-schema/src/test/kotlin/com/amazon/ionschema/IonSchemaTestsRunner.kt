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

import com.amazon.ion.IonList
import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ionschema.IonSchemaTests.isInvalidSchemasTestCase
import com.amazon.ionschema.IonSchemaTests.isInvalidTypesTestCase
import com.amazon.ionschema.IonSchemaTests.isValueTestCase
import com.amazon.ionschema.IonSchemaVersion.v1_0
import com.amazon.ionschema.IonSchemaVersion.v2_0
import com.amazon.ionschema.internal.IonSchemaSystemImpl
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.assertThrows
import java.io.File

class IonSchemaTests_1_0 : TestFactory by IonSchemaTestsRunner(v1_0)
class IonSchemaTests_2_0 : TestFactory by IonSchemaTestsRunner(
    islVersion = v2_0,
    additionalFileFilter = {
        it.path.contains("ion_schema_2_0/schema/") ||
            it.path.endsWith("constraints/all_of.isl") ||
            it.path.endsWith("constraints/any_of.isl") ||
            it.path.endsWith("constraints/byte_length.isl") ||
            it.path.endsWith("constraints/codepoint_length.isl") ||
            it.path.endsWith("constraints/container_length.isl") ||
            it.path.endsWith("constraints/contains.isl") ||
            it.path.endsWith("constraints/element.isl") ||
            it.path.endsWith("constraints/not.isl") ||
            // TODO: Add "one_of" tests once annotations support is added
            it.path.endsWith("constraints/ordered_elements.isl") ||
            it.path.endsWith("constraints/precision.isl") ||
            it.path.endsWith("constraints/timestamp_offset.isl") ||
            it.path.endsWith("constraints/timestamp_precision.isl") ||
            it.path.endsWith("constraints/type.isl") ||
            it.path.endsWith("constraints/utf8_byte_length.isl") ||
            it.path.endsWith("constraints/valid_values.isl") ||
            it.path.endsWith("constraints/valid_values-ranges.isl")
    }
)

/**
 * Primary test runner for the file-based test suite.
 * Use this to create a different class for each Ion Schema version since the IntelliJ and Html
 * reports lose all structure from the [DynamicNode] hierarchy.
 */
class IonSchemaTestsRunner(islVersion: IonSchemaVersion, additionalFileFilter: (File) -> Boolean) : TestFactory {

    companion object {
        operator fun invoke(islVersion: IonSchemaVersion) = IonSchemaTestsRunner(islVersion) { true }
    }

    private val baseDir = IonSchemaTests.testDirectoryFor(islVersion)
    private val fileFilter: (File) -> Boolean = { it.path.endsWith(".isl") && additionalFileFilter(it) }

    private val schemaSystem = IonSchemaSystemBuilder.standard()
        .withAuthority(IonSchemaTests.authorityFor(islVersion))
        .allowTransitiveImports(false)
        .build() as IonSchemaSystemImpl

    override fun generateTests(): Iterable<DynamicNode> {
        return baseDir.walk()
            .filter { it.isFile }
            .filter(fileFilter)
            .map { generateTestCases(it) }
            .asIterable()
    }

    private fun generateTestCases(f: File): DynamicNode {
        val schemaId = f.relativeTo(baseDir).path

        // First we'll try to load the test file as an Ion Schema.
        // If it doesn't load, then generate a single failing test case for this file.
        val schema: Schema = try {
            schemaSystem.loadSchema(schemaId)
        } catch (t: Throwable) {
            return dynamicTest(schemaId) { throw t }
        }

        fun getRequiredType(name: String) = requireNotNull(schema.getType(name)) { "Unable to find type '$name' in $schemaId" }

        val testCasesIon = schema.isl.filter { it.hasTypeAnnotation("\$test") }
        val dynamicNodeTestCases: List<DynamicNode> = testCasesIon.mapNotNull { ion ->
            when {
                ion !is IonStruct -> dynamicTest(schemaId) { throw IllegalArgumentException("Malformed test input: $ion") }

                isValueTestCase(ion) -> {
                    val type = schema.getType((ion["type"] as IonSymbol).stringValue()) ?: return@mapNotNull dynamicTest(schemaId) {
                        throw IllegalArgumentException("Type not found: $ion")
                    }
                    val shouldMatch = (ion["should_accept_as_valid"] as IonList? ?: emptyList<IonValue>())
                        .map { createValueTestCase(schemaId, type, it, expectValid = true) }
                    val shouldNotMatch = (ion["should_reject_as_invalid"] as IonList? ?: emptyList<IonValue>())
                        .map { createValueTestCase(schemaId, type, it, expectValid = false) }
                    dynamicContainer(schemaId, shouldMatch + shouldNotMatch)
                }

                isInvalidSchemasTestCase(ion) -> createInvalidSchemasTestCases(schemaId, ion)

                isInvalidTypesTestCase(ion) -> {
                    val baseDescription = ion.getTextField("description")
                    val cases = (ion["invalid_types"] as IonList).mapIndexed { i, it ->
                        dynamicTest("[$schemaId] $baseDescription [$i]") {
                            assertThrows<InvalidSchemaException> { schema.newType(it as IonStruct) }
                        }
                    }
                    dynamicContainer("[$schemaId] $baseDescription", cases)
                }

                else -> dynamicTest(schemaId) { throw IllegalArgumentException("Malformed test input: $ion") }
            }
        }
        return dynamicContainer(schemaId, f.toURI(), dynamicNodeTestCases.stream())
    }

    private fun createInvalidSchemasTestCases(schemaId: String, ion: IonStruct): DynamicNode {
        val baseDescription = ion.getTextField("description")
        val cases = (ion["invalid_schemas"] as IonList).mapIndexed { i, it ->
            dynamicTest("[$schemaId] $baseDescription [$i]") {
                assertThrows<InvalidSchemaException> { schemaSystem.newSchema(it.asDocument().iterator()) }
            }
        }
        return dynamicContainer("[$schemaId] $baseDescription", cases)
    }

    private fun createValueTestCase(schemaId: String, testType: Type, value: IonValue, expectValid: Boolean): DynamicNode {
        val name = "Type '${testType.name}' should${if (expectValid) " " else " not "}match value: $value"
        return dynamicTest("[$schemaId] $name") {
            val preparedValue = maybeConvertToDocument(value)
            testType.assertValidity(expectValid, preparedValue)
        }
    }
}
