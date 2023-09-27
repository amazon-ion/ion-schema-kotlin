// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal

import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.assertEqualIon
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.HeaderImport
import com.amazon.ionschema.model.SchemaHeader
import com.amazon.ionschema.model.UserReservedFields
import com.amazon.ionschema.util.bagOf
import org.junit.jupiter.api.Test

@OptIn(ExperimentalIonSchemaModel::class)
class HeaderWriterTest {
    val ION = IonSystemBuilder.standard().build()
    private val headerWriter = HeaderWriter

    @Test
    fun `writeHeader can write an empty header`() {
        assertEqualIon("schema_header::{}") { w -> headerWriter.writeHeader(w, SchemaHeader()) }
    }

    @Test
    fun `writeHeader can write a header with open content`() {
        val header = SchemaHeader(
            openContent = bagOf(
                "foo" to ION.newInt(1),
                "bar" to ION.newInt(2),
            )
        )
        assertEqualIon(
            """
            schema_header::{
              foo:1,
              bar:2
            }
        """
        ) { w -> headerWriter.writeHeader(w, header) }
    }

    @Test
    fun `writeHeader can write a header with imports`() {
        val header = SchemaHeader(
            imports = setOf(
                HeaderImport.Wildcard("foo"),
                HeaderImport.Type("bar", "type_a"),
                HeaderImport.Type("baz", "type_b", asType = "type_c"),
            ),
        )
        assertEqualIon(
            """
            schema_header::{
              imports:[
                {id:"foo"},
                {id:"bar", type: type_a},
                {id:"baz", type: type_b, as: type_c},
              ]
            }
        """
        ) { w -> headerWriter.writeHeader(w, header) }
    }

    @Test
    fun `writeHeader can write a header with user reserved fields`() {
        val header = SchemaHeader(
            userReservedFields = UserReservedFields(
                type = setOf("foo"),
                header = setOf("bar"),
                footer = setOf("baz"),
            ),
        )
        assertEqualIon(
            """
            schema_header::{
              user_reserved_fields:{
                type: [foo],
                schema_header: [bar],
                schema_footer: [baz],
              }
            }
        """
        ) { w -> headerWriter.writeHeader(w, header) }
    }
}
