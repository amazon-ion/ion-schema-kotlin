/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
import java.io.File

/**
 * Utilities for working with `ion-schema-tests`.
 */
object IonSchemaTests {
    const val ION_SCHEMA_TESTS_DIR = "../ion-schema-tests"

    val TEST_CASE_ANNOTATIONS = arrayOf("\$test")
    val VALUE_TEST_CASE_FIELDS = setOf("type", "should_accept_as_valid", "should_reject_as_invalid")
    val INVALID_SCHEMA_TEST_CASE_FIELDS = setOf("description", "invalid_schemas")
    val VALID_SCHEMA_TEST_CASE_FIELDS = setOf("description", "valid_schemas")
    val INVALID_TYPES_TEST_CASE_FIELDS = setOf("description", "invalid_types")

    /**
     * Returns a sequence of all test case files for a particular version of Ion Schema.
     */
    fun asSequence(excluding: List<String> = emptyList(), version: IonSchemaVersion): Sequence<File> {
        return testDirectoryFor(version).walk()
            .filter { it.isFile }
            .filter { it.path.endsWith(".isl") }
            .filter { it.path !in excluding }
    }

    /**
     * Returns an [AuthorityFilesystem] for a particular Ion Schema version's tests.
     */
    fun authorityFor(version: IonSchemaVersion) = AuthorityFilesystem(testDirectoryFor(version).path)

    /**
     * Returns the directory (as a [File]) that is the root for a particular Ion Schema version's tests.
     */
    fun testDirectoryFor(version: IonSchemaVersion) = File("$ION_SCHEMA_TESTS_DIR/${version.testSuiteDirectoryName}")

    /**
     * Checks if an [IonStruct] is a valid/invalid value test case
     */
    fun isValueTestCase(ion: IonStruct): Boolean {
        val fieldNames = ion.fieldNameSet
        return ion.typeAnnotations.contentEquals(TEST_CASE_ANNOTATIONS) &&
            fieldNames.contains("type") &&
            VALUE_TEST_CASE_FIELDS.containsAll(fieldNames)
    }

    fun isInvalidSchemasTestCase(ion: IonStruct): Boolean {
        return ion.typeAnnotations.contentEquals(TEST_CASE_ANNOTATIONS) &&
            ion.fieldNameSet.containsAll(INVALID_SCHEMA_TEST_CASE_FIELDS)
    }

    fun isValidSchemasTestCase(ion: IonStruct): Boolean {
        return ion.typeAnnotations.contentEquals(TEST_CASE_ANNOTATIONS) &&
            ion.fieldNameSet.containsAll(VALID_SCHEMA_TEST_CASE_FIELDS)
    }

    fun isInvalidTypesTestCase(ion: IonStruct): Boolean {
        return ion.typeAnnotations.contentEquals(TEST_CASE_ANNOTATIONS) &&
            ion.fieldNameSet.containsAll(INVALID_TYPES_TEST_CASE_FIELDS)
    }

    private val IonStruct.fieldNameSet: Set<String>
        get() = mapTo(mutableSetOf()) { it.fieldName }
}
