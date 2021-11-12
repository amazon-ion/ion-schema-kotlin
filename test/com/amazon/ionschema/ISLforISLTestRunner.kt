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

import com.amazon.ion.IonStruct
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.junit.runner.notification.RunNotifier
import org.junit.runners.Suite
import java.io.File
import java.io.FileReader

/**
 * This test runner uses the ISL for ISL itself to validate the types and schemas
 * defined in the file-based test suite.
 */
@RunWith(ISLforISLTestRunner::class)
@Suite.SuiteClasses(ISLforISLTestRunner::class)
class ISLforISLTestRunner(
    testClass: Class<Any>
) : AbstractTestRunner(testClass) {

    private val schemaSystem = IonSchemaSystemBuilder.standard()
        .withAuthority(AuthorityFilesystem("ion-schema-schemas"))
        .allowTransitiveImports(false)
        .build()

    // There are certain conditions that make a schema invalid that cannot
    // be detected by the ISL for ISL (for example, a schema importing itself).
    // Test files with "invalid_schema" definitions should be listed here if and
    // only if they are invalid for some reason that cannot be validated by ISL.
    private val blacklist = setOf(
        "ion-schema-tests/schema/import/import_type_unknown.isl",
        "ion-schema-tests/schema/import/invalid_duplicate_import.isl",
        "ion-schema-tests/schema/import/invalid_duplicate_import_type.isl",
        "ion-schema-tests/schema/import/invalid_duplicate_type.isl",
        "ion-schema-tests/schema/import/invalid_transitive_import_of_schema.isl",
        "ion-schema-tests/schema/import/invalid_transitive_import_of_type.isl",
        "ion-schema-tests/schema/import/invalid_transitive_import_of_type_by_alias.isl",
        "ion-schema-tests/schema/invalid_missing_schema_footer.isl",
        "ion-schema-tests/schema/invalid_missing_schema_header.isl",
        "ion-schema-tests/schema/invalid_reuse_of_type_name.isl",
        "ion-schema-tests/schema/invalid_unknown_type.isl"
    )

    override fun run(notifier: RunNotifier) {
        val islSchema = schemaSystem.loadSchema("isl/schema.isl")
        val schema = islSchema.getType("schema")!!
        val type = islSchema.getType("type")!!

        val base = "ion-schema-tests"
        File(base).walk()
            .filter { it.isFile }
            .filter { it.path.endsWith(".isl") }
            .filter { !blacklist.contains(it.path) }
            .forEach { file ->
                val testName = file.path.substring(base.length + 1, file.path.length - ".isl".length)

                val iter = ION.iterate(FileReader(file)).asSequence().toList().listIterator()
                iter.forEach { ion ->
                    val annotation = ion.typeAnnotations[0]
                    when (annotation) {
                        "schema_header" -> {
                            iter.previous()
                            val datagram = ION.newDatagram()
                            do {
                                val v = iter.next()
                                datagram.add(v)
                            } while (iter.hasNext() && !v.hasTypeAnnotation("schema_footer"))

                            runTest(notifier, testName, datagram) {
                                println(datagram)
                                val result = schema.validate(datagram)
                                println(result)
                                assertTrue(result.isValid())
                            }
                        }

                        "invalid_schema" ->
                            runTest(notifier, testName, ion) {
                                val value = prepareValue(ion)
                                println(value)
                                val result = schema.validate(value)
                                println(result)
                                assertFalse(result.isValid())
                            }

                        "type" ->
                            runTest(notifier, testName, ion) {
                                ion as IonStruct
                                // ensure the type has a name, otherwise it's not valid ISL
                                ion["name"] ?: ion.put("name", ION.newSymbol(testName))
                                println(ion)
                                val result = type.validate(ion)
                                println(result)
                                assertTrue(result.isValid())
                            }

                        "invalid_type" ->
                            runTest(notifier, testName, ion) {
                                println(ion)
                                val result = type.validate(ion)
                                println(result)
                                assertFalse(result.isValid())
                            }
                    }
                }
            }
    }
}
