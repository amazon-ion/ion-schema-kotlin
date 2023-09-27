// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal

import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.assertEqualIon
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.SchemaFooter
import com.amazon.ionschema.util.bagOf
import org.junit.jupiter.api.Test

@OptIn(ExperimentalIonSchemaModel::class)
class FooterWriterTest {

    val ION = IonSystemBuilder.standard().build()
    private val footerWriter = FooterWriter

    @Test
    fun `writeFooter can write an empty footer`() {
        assertEqualIon("schema_footer::{}") { w -> footerWriter.writeFooter(w, SchemaFooter()) }
    }

    @Test
    fun `writeFooter can write a footer with open content`() {
        val footer = SchemaFooter(
            openContent = bagOf(
                "foo" to ION.newInt(1),
                "bar" to ION.newInt(2),
            )
        )
        assertEqualIon("schema_footer::{foo:1,bar:2}") { w -> footerWriter.writeFooter(w, footer) }
    }
}
