/*
 * Copyright 2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.ionschema.internal

import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.IonSchemaVersion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SchemaContentTest {

    val ION = IonSystemBuilder.standard().build()

    @Test
    fun `SchemaContent determines the correct version for $ion_schema_1_0`() {
        val ion = ION.loader.load("\$ion_schema_1_0")
        val schemaContent = SchemaContent(ion)
        assertEquals(IonSchemaVersion.v1_0, schemaContent.version)
    }

    @Test
    fun `SchemaContent determines the correct version for $ion_schema_2_0`() {
        val ion = ION.loader.load("\$ion_schema_2_0")
        val schemaContent = SchemaContent(ion)
        assertEquals(IonSchemaVersion.v2_0, schemaContent.version)
    }

    @Test
    fun `SchemaContent determines the correct version when no version marker`() {
        val ion = ION.loader.load("foo bar baz")
        val schemaContent = SchemaContent(ion)
        assertEquals(IonSchemaVersion.v1_0, schemaContent.version)
    }

    @Test
    fun `SchemaContent lists the declared types (regardless of syntactic validity)`() {
        val expectedTypes = listOf("foo", "bar", "baz")

        val schemaContent = SchemaContent(
            expectedTypes.map {
                ION.singleValue("type::{ name: $it, content:(not-closed), occurs: range::['π', max] }")
            }
        )

        assertEquals(expectedTypes, schemaContent.declaredTypes.map { it.stringValue() })
    }
}
