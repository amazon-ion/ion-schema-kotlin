package com.amazon.ionschema.internal.util

import com.amazon.ion.IonContainer
import com.amazon.ion.IonInt
import com.amazon.ion.IonList
import com.amazon.ion.IonString
import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.InvalidSchemaException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PreconditionsTest {

    private val ion = IonSystemBuilder.standard().build()

    @Nested
    inner class islRequire {
        @Test
        fun `when value is true, should return normally`() {
            islRequire(true) { "Message" }
        }

        @Test
        fun `when value is false, should throw exception`() {
            assertThrows<InvalidSchemaException> {
                islRequire(false) { "Message" }
            }.also {
                assertEquals("Message", it.message)
            }
        }

        @Test
        fun `compiler should infer things based on function contract`() {
            val anyValue: Any = Unit
            islRequire(anyValue is Unit) { "Message" }
            // This is essentially a compile-time test. If the contract isn't working, then
            // the compiler cannot make a smart cast of `anyValue` from Any to Unit.
            return anyValue
        }
    }

    @Nested
    inner class islRequireNotNull {
        @Test
        fun `when value is not null, should return normally`() {
            val maybeNull: Any? = Unit
            val result = islRequireNotNull(maybeNull) { "Message" }
            assertSame(maybeNull, result)
        }

        @Test
        fun `when value is null, should throw exception`() {
            val maybeNull: Any? = null
            assertThrows<InvalidSchemaException> {
                islRequireNotNull(maybeNull) { "Message" }
            }.also {
                assertEquals("Message", it.message)
            }
        }

        @Test
        fun `compiler should infer things based on function contract`() {
            val maybeNull: Unit? = Unit
            islRequireNotNull(maybeNull) { "Message" }
            // This is essentially a compile-time test. If the contract isn't working, then
            // the compiler cannot make a smart cast of `maybeNull` from Unit? to Unit.
            return maybeNull
        }
    }

    @Nested
    inner class islRequireIonNotNull {
        @Test
        fun `when value is not null, should return normally`() {
            val maybeNull: IonValue? = ion.singleValue("1")
            val result = islRequireIonNotNull(maybeNull) { "Message" }
            assertSame(maybeNull, result)
        }

        @Test
        fun `when value is an Ion null, should throw exception`() {
            val maybeNull: IonValue? = ion.singleValue("null.int")
            assertThrows<InvalidSchemaException> {
                islRequireIonNotNull(maybeNull) { "Message" }
            }.also {
                assertEquals("Message", it.message)
            }
        }

        @Test
        fun `when value is null, should throw exception`() {
            val maybeNull: IonValue? = null
            assertThrows<InvalidSchemaException> {
                islRequireIonNotNull(maybeNull) { "Message" }
            }.also {
                assertEquals("Message", it.message)
            }
        }

        @Test
        fun `compiler should infer things based on function contract`() {
            val maybeNull: IonValue? = ion.singleValue("1")
            islRequireIonNotNull(maybeNull) { "Message" }
            // This is essentially a compile-time test. If the contract isn't working, then
            // the compiler cannot make a smart cast of `maybeNull` from IonValue? to IonValue.
            val notNull: IonValue = maybeNull
        }
    }

    @Nested
    inner class islRequireIonTypeNotNull {
        @Test
        fun `when value is correct Ion type and not null, should return normally`() {
            val maybeIonInt: IonValue? = ion.singleValue("1")
            val result = islRequireIonTypeNotNull<IonInt>(maybeIonInt) { "Message" }
            assertSame(maybeIonInt, result)
        }

        @Test
        fun `when value is wrong Ion type, should throw exception`() {
            val maybeIonInt: IonValue? = ion.singleValue("1.0")
            assertThrows<InvalidSchemaException> {
                islRequireIonTypeNotNull<IonInt>(maybeIonInt) { "Message" }
            }.also {
                assertEquals("Message", it.message)
            }
        }

        @Test
        fun `when value is an Ion null, should throw exception`() {
            val maybeIonInt: IonValue? = ion.singleValue("null.int")
            assertThrows<InvalidSchemaException> {
                islRequireIonTypeNotNull<IonInt>(maybeIonInt) { "Message" }
            }.also {
                assertEquals("Message", it.message)
            }
        }

        @Test
        fun `when value is null, should throw exception`() {
            val maybeIonInt: IonValue? = null
            assertThrows<InvalidSchemaException> {
                islRequireIonTypeNotNull<IonInt>(maybeIonInt) { "Message" }
            }.also {
                assertEquals("Message", it.message)
            }
        }

        @Test
        fun `compiler should infer things based on function contract`() {
            val maybeIonInt: IonValue? = ion.singleValue("1")
            islRequireIonTypeNotNull<IonInt>(maybeIonInt) { "Message" }
            // This is essentially a compile-time test. If the contract isn't working, then
            // the compiler cannot make a smart cast of `maybeIonInt` from IonValue? to IonInt.
            val ionInt: IonInt = maybeIonInt
        }
    }

    @Nested
    inner class islRequireElementType {
        @Test
        fun `when collection is empty, should return empty list`() {
            expectOk("[]") {
                it.islRequireElementType<IonString>("a list")
            }
        }

        @Test
        fun `when collection has null value and nulls not allowed, should throw`() {
            assertThrows<InvalidSchemaException> {
                (ion.singleValue(" [a, null.symbol, b] ") as IonList)
                    .islRequireElementType<IonString>("my cool list")
            }.also {
                assertTrue("my cool list" in it.message!!)
            }
        }

        @Test
        fun `when collection has null value and nulls are allowed, should return normally`() {
            expectOk(" [a, null.symbol, b] ") {
                it.islRequireElementType<IonSymbol>("my cool list", allowIonNulls = true)
            }
        }

        @Test
        fun `when collection has annotated value and annotations not allowed, should throw`() {
            assertThrows<InvalidSchemaException> {
                (ion.singleValue(" [a, foo::b, c] ") as IonList)
                    .islRequireElementType<IonString>("my cool list")
            }.also {
                assertTrue("my cool list" in it.message!!)
            }
        }

        @Test
        fun `when collection has annotated value and annotations are allowed, should return normally`() {
            expectOk(" [a, foo::b, c] ") {
                it.islRequireElementType<IonSymbol>("my cool list", allowAnnotations = true)
            }
        }

        private fun expectOk(ionText: String, fn: (Iterable<IonValue>) -> Iterable<IonValue>) {
            val theList = (ion.singleValue(ionText) as IonContainer)
            val result = fn(theList)
            assertSame(theList, result)
        }
    }

    @Nested
    inner class getIslRequiredField {
        @Test
        fun `when field is right type, should return`() {
            val result = (ion.singleValue(" {a:1} ") as IonStruct)
                .getIslRequiredField<IonInt>("a")

            assertEquals(1, result.intValue())
        }

        @Test
        fun `when field is wrong type, should throw`() {
            assertThrows<InvalidSchemaException> {
                (ion.singleValue(" {a:1.23} ") as IonStruct)
                    .getIslRequiredField<IonInt>("a")
            }
        }

        @Test
        fun `when field is not present, should throw`() {
            assertThrows<InvalidSchemaException> {
                (ion.singleValue(" {a:1} ") as IonStruct)
                    .getIslRequiredField<IonInt>("b")
            }
        }

        @Test
        fun `when field is repeated, should throw`() {
            assertThrows<InvalidSchemaException> {
                (ion.singleValue(" {a:1, a:1} ") as IonStruct)
                    .getIslRequiredField<IonInt>("a")
            }
        }

        @Test
        fun `when field is a null value and nulls not allowed, should throw`() {
            assertThrows<InvalidSchemaException> {
                (ion.singleValue(" {a:null.int} ") as IonStruct)
                    .getIslRequiredField<IonInt>("a")
            }
        }

        @Test
        fun `when field is a null value and nulls are allowed, should return ion null`() {
            val result = (ion.singleValue(" {a:null.int} ") as IonStruct)
                .getIslRequiredField<IonInt>("a", allowIonNulls = true)

            assertTrue(result.isNullValue)
        }

        @Test
        fun `when field has annotations and annotations not allowed, should throw`() {
            assertThrows<InvalidSchemaException> {
                (ion.singleValue(" {a:foo::1} ") as IonStruct)
                    .getIslRequiredField<IonInt>("a")
            }
        }

        @Test
        fun `when field has annotations and annotations are allowed, should return`() {
            val result = (ion.singleValue(" {a:foo::1} ") as IonStruct)
                .getIslRequiredField<IonInt>("a", allowAnnotations = true)

            assertTrue(result.hasTypeAnnotation("foo"))
            assertEquals(1, result.intValue())
        }
    }

    @Nested
    inner class getIslOptionalField {
        @Test
        fun `when field is right type, should return`() {
            val result = (ion.singleValue(" {a:1} ") as IonStruct)
                .getIslOptionalField<IonInt>("a")

            result!! // Asserts that result is not null
            assertEquals(1, result.intValue())
        }

        @Test
        fun `when field is wrong type, should throw`() {
            assertThrows<InvalidSchemaException> {
                (ion.singleValue(" {a:1.23} ") as IonStruct)
                    .getIslOptionalField<IonInt>("a")
            }
        }

        @Test
        fun `when field is not present, should return null`() {
            val result = (ion.singleValue(" {a:1} ") as IonStruct)
                .getIslOptionalField<IonInt>("b")

            assertEquals(null, result)
        }

        @Test
        fun `when field is repeated, should throw`() {
            assertThrows<InvalidSchemaException> {
                (ion.singleValue(" {a:1, a:1} ") as IonStruct)
                    .getIslOptionalField<IonInt>("a")
            }
        }

        @Test
        fun `when field is a null value and nulls not allowed, should throw`() {
            assertThrows<InvalidSchemaException> {
                (ion.singleValue(" {a:null.int} ") as IonStruct)
                    .getIslOptionalField<IonInt>("a")
            }
        }

        @Test
        fun `when field is a null value and nulls are allowed, should return ion null`() {
            val result = (ion.singleValue(" {a:null.int} ") as IonStruct)
                .getIslOptionalField<IonInt>("a", allowIonNulls = true)

            result!! // Asserts that result is not null
            assertTrue(result.isNullValue)
        }

        @Test
        fun `when field has annotations and annotations not allowed, should throw`() {
            assertThrows<InvalidSchemaException> {
                (ion.singleValue(" {a:foo::1} ") as IonStruct)
                    .getIslOptionalField<IonInt>("a")
            }
        }

        @Test
        fun `when field has annotations and annotations are allowed, should return`() {
            val result = (ion.singleValue(" {a:foo::1} ") as IonStruct)
                .getIslOptionalField<IonInt>("a", allowAnnotations = true)

            result!! // Asserts that result is not null
            assertTrue(result.hasTypeAnnotation("foo"))
            assertEquals(1, result.intValue())
        }
    }

    @Nested
    inner class islRequireOnlyExpectedFieldNames {

        @Test
        fun `when no fields, should return normally`() {
            val struct = (ion.singleValue("{}") as IonStruct)
            val result = struct.islRequireOnlyExpectedFieldNames(listOf("a"))
            assertSame(struct, result)
        }

        @Test
        fun `when only expected fields, should return normally`() {
            val struct = (ion.singleValue("{a:1}") as IonStruct)
            val result = struct.islRequireOnlyExpectedFieldNames(listOf("a"))
            assertSame(struct, result)
        }

        @Test
        fun `when expected field is repeated, should return normally`() {
            val struct = (ion.singleValue("{a:1, a:2}") as IonStruct)
            val result = struct.islRequireOnlyExpectedFieldNames(listOf("a"))
            assertSame(struct, result)
        }

        @Test
        fun `when unexpected field is present, should throw`() {
            val struct = (ion.singleValue("{a:1, b:2}") as IonStruct)
            assertThrows<InvalidSchemaException> {
                struct.islRequireOnlyExpectedFieldNames(listOf("a"))
            }
        }
    }
}
