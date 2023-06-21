package com.amazon.ionschema.internal.constraint

import com.amazon.ionschema.ION
import com.amazon.ionschema.IonSchemaSystemBuilder
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class OneOfTest {
    val iss = IonSchemaSystemBuilder.standard().build()

    @Test
    fun issue278() {
        // Reproduction of https://github.com/amazon-ion/ion-schema-kotlin/issues/278
        val schemaText = """
            ${'$'}ion_schema_2_0
            type::{
              name: foo,
              one_of: [foo_a, foo_b]
            }

            type::{
              name: foo_a,
              type: struct,
              fields: {
                id: { type: string, occurs: required },
              }
            }

            type::{
              name: foo_b,
              type: struct,
              fields: {
                digest: { type: string, occurs: required },
              }
            }
        """

        val schema = iss.newSchema(schemaText)
        val type = schema.getType("foo")!!

        // In issue #278, this was throwing a ClassCastException.
        // This should return normally and indicate that the value is invalid.
        val result = type.validate(ION.singleValue("""{ id: "abc", digest: "def" }"""))
        assertFalse(result.isValid())
    }
}
