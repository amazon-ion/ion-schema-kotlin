package com.amazon.ionschema.reader.internal

import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.internal.util.IonSchema_2_0
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.SchemaFooter
import com.amazon.ionschema.model.UserReservedFields
import com.amazon.ionschema.util.bagOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalIonSchemaModel::class)
class FooterReaderTest {

    val ION = IonSystemBuilder.standard().build()
    private val footerReader = FooterReader { it in userReservedFields.footer || !IonSchema_2_0.RESERVED_WORDS_REGEX.matches(it) }

    @Test
    fun `readFooter can read an empty footer`() {
        val context = ReaderContext()
        val footer = footerReader.readFooter(context, ION.singleValue("schema_footer::{}"))
        assertEquals(SchemaFooter(), footer)
    }

    @Test
    fun `readFooter can read a footer with unreserved open content`() {
        val context = ReaderContext()
        val footer = footerReader.readFooter(context, ION.singleValue("schema_footer::{_foo:1,_bar:2}"))

        val expected = SchemaFooter(
            openContent = bagOf(
                "_foo" to ION.newInt(1),
                "_bar" to ION.newInt(2),
            )
        )

        assertEquals(expected, footer)
    }

    @Test
    fun `readFooter can read a footer with a user reserved field as open content`() {
        val context = ReaderContext()
        context.userReservedFields = UserReservedFields(footer = setOf("foo", "bar"))
        val footer = footerReader.readFooter(context, ION.singleValue("schema_footer::{foo:1,bar:2}"))
        val expected = SchemaFooter(
            openContent = bagOf(
                "foo" to ION.newInt(1),
                "bar" to ION.newInt(2),
            )
        )

        assertEquals(expected, footer)
    }

    @Test
    fun `readFooter throws exception when illegal field is open content`() {
        val context = ReaderContext()
        assertThrows<InvalidSchemaException> { footerReader.readFooter(context, ION.singleValue("schema_footer::{type:1}")) }
    }
}
