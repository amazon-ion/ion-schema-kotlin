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

import com.amazon.ion.IonContainer
import com.amazon.ion.IonList
import com.amazon.ion.IonSequence
import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ion.system.IonTextWriterBuilder
import com.amazon.ionschema.internal.IonSchemaSystemImpl
import com.amazon.ionschema.internal.SchemaCore
import com.amazon.ionschema.internal.SchemaImpl
import com.amazon.ionschema.internal.TypeImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.fail
import org.junit.runner.RunWith
import org.junit.runner.notification.RunNotifier
import org.junit.runners.Suite
import java.io.File
import java.io.FileReader
import java.io.OutputStream

/**
 * Primary test runner for the file-based test suite.
 */
@RunWith(IonSchemaTestRunner::class)
@Suite.SuiteClasses(IonSchemaTestRunner::class)
class IonSchemaTestRunner(
    testClass: Class<Any>
) : AbstractTestRunner(testClass) {

    private val schemaSystem = IonSchemaSystemBuilder.standard()
        .withAuthority(AuthorityFilesystem("ion-schema-tests"))
        .allowTransitiveImports(false)
        .build()

    private val schemaCore = SchemaCore(schemaSystem)

    private val blacklist = setOf("")

    private val specialFieldNames = setOf("fields", "element")

    override fun run(notifier: RunNotifier) {
        val base = "ion-schema-tests"
        File(base).walk()
            .filter { it.isFile }
            .filter { it.path.endsWith(".isl") }
            .filter { !blacklist.contains(it.path) }
            .forEach { file ->
                val testName = file.path.substring(base.length + 1, file.path.length - ".isl".length)
                val testFile = file.path.substring(base.length + 1, file.path.length)
                var schema: Schema? = null
                var type: Type? = null

                val testFileIon = ION.iterate(FileReader(file)).asSequence().toList()

                val iter = when (testFileIon.count { it.hasTypeAnnotation("schema_header") }) {
                    0 -> testFileIon.listIterator()
                    1 -> {
                        val schemaIon = testFileIon.dropWhile { !it.hasTypeAnnotation("schema_header") }.dropLastWhile { !it.hasTypeAnnotation("schema_footer") }
                        val testCasesIon = testFileIon.takeWhile { !it.hasTypeAnnotation("schema_header") } + testFileIon.takeLastWhile { !it.hasTypeAnnotation("schema_footer") }
                        schema = SchemaImpl(schemaSystem as IonSchemaSystemImpl, schemaCore, schemaIon.iterator(), testFile)
                        testCasesIon.listIterator()
                    }
                    else -> throw IllegalArgumentException("IonSchemaTestRunner does not support multiple valid schema definitions in a single test file.")
                }

                iter.forEach { ion ->
                    val annotation = ion.typeAnnotations[0]
                    when (annotation) {
                        "type" -> type = TypeImpl(ion as IonStruct, schemaCore)

                        "valid", "invalid" -> {
                            val expectValid = annotation == "valid"
                            (ion as IonContainer).forEach {
                                if (it.fieldName != null) {
                                    val testType = schema!!.getType(it.fieldName)
                                    testType ?: throw Exception("Unrecognized type name '${it.fieldName}'")
                                    (it as IonSequence).forEach {
                                        val value = prepareValue(it)
                                        runTest(notifier, testName, value) {
                                            val violations = testType.validate(value)
                                            println(violations)
                                            assertEquals(expectValid, violations.isValid())
                                            assertEquals(expectValid, testType.isValid(value))
                                        }
                                    }
                                } else {
                                    if (type == null) {
                                        throw Exception("No type defined for test $testName")
                                    }
                                    val value = prepareValue(it)
                                    runTest(notifier, testName, value) {
                                        val violations = type!!.validate(value)
                                        println(violations)
                                        assertEquals(expectValid, violations.isValid())
                                        assertEquals(expectValid, type!!.isValid(value))
                                    }
                                }
                            }
                        }

                        "invalid_schema" -> {
                            runTest(notifier, testName, ion) {
                                try {
                                    SchemaImpl(
                                        schemaSystem as IonSchemaSystemImpl, schemaCore,
                                        (prepareValue(ion) as IonSequence).iterator(), testName
                                    )
                                    fail("Expected an InvalidSchemaException")
                                } catch (e: InvalidSchemaException) {
                                }
                            }
                        }

                        "invalid_type" -> {
                            runTest(notifier, testName, ion) {
                                try {
                                    TypeImpl(ion as IonStruct, schemaCore)
                                    fail("Expected an InvalidSchemaException")
                                } catch (e: InvalidSchemaException) {
                                }
                            }
                        }

                        "test_validation" -> {
                            if (ion is IonStruct) {
                                val validationType =
                                    if (ion["type"] != null) {
                                        schema!!.getType((ion["type"] as IonSymbol).stringValue())
                                    } else {
                                        type
                                    }

                                val theValue = ion.get("value")
                                val theValues = ion.get("values")
                                if (theValue == null && theValues == null) {
                                    throw Exception("Expected either 'value' or 'values' to be specified:  $ion")
                                }

                                val testValues = mutableListOf<IonValue>()
                                theValue?.let { testValues.add(it) }
                                theValues?.let { testValues.addAll(it as IonSequence) }

                                testValues.forEach {
                                    val value = prepareValue(it)
                                    runTest(notifier, testName, value) {
                                        val violations = validationType!!.validate(value)
                                        if (!violations.isValid()) {
                                            println(violations)
                                            val writer = IonTextWriterBuilder.pretty().build(System.out as OutputStream)
                                            violations.toIon().writeTo(writer)
                                        }
                                        assertEquals(ion.get("violations"), violations.toIon())
                                        assertFalse(validationType.isValid(value))
                                    }
                                }
                            }
                        }

                        else -> throw Exception("Unrecognized annotation '$annotation' in ${file.path}")
                    }
                }
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
        if (value != null && value != null) {
            struct.put("value", (value as IonValue).clone())
        }
        addNestedData(struct, this)
        return struct
    }
}
