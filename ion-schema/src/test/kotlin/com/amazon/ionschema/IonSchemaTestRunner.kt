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
import com.amazon.ion.IonSequence
import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ion.system.IonTextWriterBuilder
import com.amazon.ionschema.IonSchemaTestFilesSource.Companion.ION_SCHEMA_TESTS_DIR
import com.amazon.ionschema.internal.IonSchemaSystemImpl
import com.amazon.ionschema.internal.SchemaCore
import com.amazon.ionschema.internal.SchemaImpl
import com.amazon.ionschema.internal.TypeImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.io.FileReader
import java.io.OutputStream

/**
 * Primary test runner for the file-based test suite.
 */
class IonSchemaTestRunner {

    private val schemaSystem = IonSchemaSystemBuilder.standard()
        .withAuthority(AuthorityFilesystem(ION_SCHEMA_TESTS_DIR))
        .allowTransitiveImports(false)
        .build() as IonSchemaSystemImpl

    private val schemaCore = SchemaCore(schemaSystem)

    private val specialFieldNames = setOf("fields", "element")

    @TestFactory
    fun generateIonSchemaTestSuite(): Iterable<DynamicNode> {
        return IonSchemaTestFilesSource.asSequence().map { generateTestCases(it) }.asIterable()
    }

    private fun generateTestCases(file: File): DynamicNode {
        val testFile = file.relativeTo(File(ION_SCHEMA_TESTS_DIR)).path
        val testFileIon = ION.iterate(FileReader(file)).asSequence().toList()

        var schema: Schema? = null

        val testCasesIon = when (testFileIon.count { it.hasTypeAnnotation("schema_header") }) {
            0 -> testFileIon
            1 -> {
                val schemaIon = testFileIon.dropWhile { !it.hasTypeAnnotation("schema_header") }.dropLastWhile { !it.hasTypeAnnotation("schema_footer") }
                val testCasesIon = testFileIon.takeWhile { !it.hasTypeAnnotation("schema_header") } + testFileIon.takeLastWhile { !it.hasTypeAnnotation("schema_footer") }
                schema = SchemaImpl(schemaSystem, schemaCore, schemaIon.iterator(), testFile)
                testCasesIon
            }
            else -> throw IllegalArgumentException("IonSchemaTestRunner does not support multiple valid schema definitions in a single test file.")
        }

        var lastSeenType: Type? = null

        val dynamicNodeTestCases: List<DynamicNode> = testCasesIon.mapNotNull { ion ->
            when (val annotation = ion.typeAnnotations[0]) {
                "type" -> {
                    lastSeenType = TypeImpl(ion as IonStruct, schemaCore)
                    null
                }

                "valid", "invalid" -> dynamicContainer(
                    testFile,
                    when (ion) {
                        is IonStruct -> ion.flatMap { casesForType ->
                            val testType = requireNotNull(schema?.getType(casesForType.fieldName)) { "[$testFile] Unrecognized type name '${casesForType.fieldName}'" }
                            (casesForType as IonList).map { createValueTestCase(testType, it, file, expectValid = (annotation == "valid")) }
                        }
                        else -> {
                            ion as IonList
                            val testType = requireNotNull(lastSeenType) { "No type defined for test $testFile" }
                            ion.map { createValueTestCase(testType, it, file, expectValid = (annotation == "valid")) }
                        }
                    }
                )

                "invalid_schema" -> dynamicTest("[$testFile] schema should be invalid: $ion") {
                    assertThrows<InvalidSchemaException> {
                        SchemaImpl(schemaSystem, schemaCore, (prepareValue(ion) as IonSequence).iterator(), testFile)
                    }
                }

                "invalid_type" -> dynamicTest("[$testFile] type should be invalid: $ion") {
                    assertThrows<InvalidSchemaException> { TypeImpl(ion as IonStruct, schemaCore) }
                }

                "test_validation" -> {
                    require(ion is IonStruct)
                    val validationType = ion["type"]?.let { schema!!.getType((it as IonSymbol).stringValue()) } ?: lastSeenType
                    requireNotNull(validationType) { "[$testFile] Validation type not specified: $ion" }
                    val testValues: MutableList<IonValue> = ion["values"] as? IonSequence ?: mutableListOf()
                    ion["value"]?.let { testValues += it }
                    require(testValues.isNotEmpty()) { "[$testFile] Expected either 'value' or 'values' to be specified:  $ion" }

                    dynamicContainer(
                        testFile,
                        testValues.map {
                            val value = prepareValue(it)
                            dynamicTest("[$testFile] test validation outputs for ${validationType.name}, value: $value") {
                                val violations = validationType.validate(value)
                                if (!violations.isValid()) {
                                    println(violations)
                                    val writer = IonTextWriterBuilder.pretty().build(System.out as OutputStream)
                                    violations.toIon().writeTo(writer)
                                }
                                assertEquals(ion.get("violations"), violations.toIon())
                                assertFalse(validationType.isValid(value))
                            }
                        }
                    )
                }
                else -> throw Exception("Unrecognized annotation '$annotation' in ${file.path}")
            }
        }
        return dynamicContainer(testFile, file.toURI(), dynamicNodeTestCases.stream())
    }

    private fun createValueTestCase(testType: Type, value: IonValue, file: File, expectValid: Boolean): DynamicNode {
        val testFile = file.relativeTo(File(ION_SCHEMA_TESTS_DIR)).path
        val name = "Type '${testType.name}' should${if (expectValid) " " else " not "}accept value: $value"
        return dynamicTest("[$testFile] $name", file.absoluteFile.toURI()) {
            val preparedValue = prepareValue(value)
            val violations = testType.validate(preparedValue).also { println(it) }
            assertEquals(expectValid, violations.isValid())
            assertEquals(expectValid, testType.isValid(preparedValue))
        }
    }

    private fun Violations.toIon(): IonList {
        val list = ION.newEmptyList()
        this.forEach {
            list.add(it.toIon())
        }
        return list
    }

    private fun addNestedData(struct: IonStruct, violation: Violations) {
        if (violation.violations.size > 0) {
            val violationList = ION.newEmptyList()
            violation.violations.forEach {
                violationList.add(it.toIon())
            }
            struct.put("violations", violationList)
        }

        if (violation.children.size > 0) {
            val childList = ION.newEmptyList()
            violation.children.forEach {
                childList.add(it.toIon())
            }
            struct.put("children", childList)
        }
    }

    private fun Violation.toIon(): IonStruct {
        val struct = ION.newEmptyStruct()
        if (constraint != null) {
            val constr = constraint as IonValue
            if (constr is IonStruct &&
                !specialFieldNames.contains(constr.fieldName)
            ) {
                struct.put("constraint", constr.clone())
            } else {
                val constraintStruct = ION.newEmptyStruct()
                constraintStruct.put(
                    constr.fieldName ?: "type",
                    constr.clone()
                )
                struct.put("constraint", constraintStruct)
            }
        }
        if (code != null) {
            struct.put("code", ION.newSymbol(code))
        }
        addNestedData(struct, this)
        return struct
    }

    private fun ViolationChild.toIon(): IonStruct {
        val struct = ION.newEmptyStruct()
        if (fieldName != null && !fieldName.equals("")) {
            struct.put("fieldName", ION.newString(fieldName))
        }
        if (index != null) {
            struct.put("index", ION.newInt(index as Int))
        }
        if (value != null) {
            struct.put("value", (value as IonValue).clone())
        }
        addNestedData(struct, this)
        return struct
    }
}
