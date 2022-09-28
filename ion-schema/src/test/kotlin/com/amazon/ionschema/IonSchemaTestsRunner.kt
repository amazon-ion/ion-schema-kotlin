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
import com.amazon.ionschema.IonSchemaTests.isInvalidSchemaTestCase
import com.amazon.ionschema.IonSchemaTests.isInvalidTypesTestCase
import com.amazon.ionschema.IonSchemaTests.isValueTestCase
import com.amazon.ionschema.IonSchemaVersion.ION_SCHEMA_1_0
import com.amazon.ionschema.internal.IonSchemaSystemImpl
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.assertThrows
import java.io.File

class IonSchemaTests_1_0 : TestFactory by IonSchemaTestsRunner(ION_SCHEMA_1_0)

/**
 * Primary test runner for the file-based test suite.
 * Use this to create a different class for each Ion Schema version since the IntelliJ and Html
 * reports lose all structure from the [DynamicNode] hierarchy.
 */
class IonSchemaTestsRunner(islVersion: IonSchemaVersion) : TestFactory {

    private val baseDir = IonSchemaTests.testDirectoryFor(islVersion)
    private val fileFilter: (File) -> Boolean = { it.path.endsWith(".isl") }

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

                isInvalidSchemaTestCase(ion) -> createInvalidSchemaTestCase(schemaId, ion)

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

    private fun createInvalidSchemaTestCase(schemaId: String, testCaseIon: IonStruct): DynamicNode {
        return dynamicTest("[$schemaId] ${testCaseIon.getTextField("description")}") {
            assertThrows<InvalidSchemaException> {
                schemaSystem.newSchema(testCaseIon["invalid_schema"].asDocument().iterator())
            }
        }
    }

    private fun createValueTestCase(schemaId: String, testType: Type, value: IonValue, expectValid: Boolean): DynamicNode {
        val name = "Type '${testType.name}' should${if (expectValid) " " else " not "}match value: $value"
        return dynamicTest("[$schemaId] $name") {
            val preparedValue = maybeConvertToDocument(value)
            testType.assertValidity(expectValid, preparedValue)
        }
    }
}
