package com.amazon.ionschema.internal.util

import com.amazon.ion.IonInt
import com.amazon.ion.IonValue
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.InvalidSchemaException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertSame

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
}
