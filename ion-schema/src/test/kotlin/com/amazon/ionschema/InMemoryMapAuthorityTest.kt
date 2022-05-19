package com.amazon.ionschema

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class InMemoryMapAuthorityTest {

    @Test
    fun unknownSchemaId() {
        val iss = IonSchemaSystemBuilder.standard().build()
        val authority = InMemoryMapAuthority.fromIonText(
            "my_schema" to """
                type::{
                  name: int_list,
                  type: list,
                  element: int
                }
            """
        )
        val iter = authority.iteratorFor(iss, "unknown_schema_id")
        assertFalse(iter.hasNext())
        try {
            iter.next()
            fail()
        } catch (e: NoSuchElementException) {
        }
        iter.close()
    }

    @Test
    fun fromIonText_knownSchemaId() {
        val iss = IonSchemaSystemBuilder.standard().build()
        val authority = InMemoryMapAuthority.fromIonText(
            "my_schema" to """
                type::{
                  name: int_list,
                  type: list,
                  element: int
                }
            """
        )
        val iter = authority.iteratorFor(iss, "my_schema")
        assertTrue(iter.hasNext())
        iter.close()
    }

    @Test
    fun fromIonValues_knownSchemaId() {
        val iss = IonSchemaSystemBuilder.standard().build()
        val authority = InMemoryMapAuthority.fromIonValues(
            "my_schema" to iss.ionSystem.loader.load(
                """
                type::{
                  name: int_list,
                  type: list,
                  element: int
                }
            """
            )!!
        )
        val iter = authority.iteratorFor(iss, "my_schema")
        assertTrue(iter.hasNext())
        iter.close()
    }
}
