package com.amazon.ionschema.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ConsistentDecimalTest {

    @Test
    fun `compareTo, hashCode, and equals are consistent for fractional numbers with different precision`() {
        val a = ConsistentDecimal(BigDecimal("20.100000"))
        val b = ConsistentDecimal(BigDecimal("20.1"))

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals(0, a.compareTo(b))
        assertEquals("$a", "$b")
        println("$a == $b")
    }

    @Test
    fun `compareTo, hashCode, and equals are consistent for integers with different precision`() {
        val a = ConsistentDecimal(BigDecimal("10.00000"))
        val b = ConsistentDecimal(BigDecimal("10"))

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals(0, a.compareTo(b))
        assertEquals("$a", "$b")
        println("$a == $b")
    }

    @Test
    fun `compareTo, hashCode, and equals are consistent for zeros with different precision`() {
        val a = ConsistentDecimal(BigDecimal("0.00000"))
        val b = ConsistentDecimal(BigDecimal("0"))

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals(0, a.compareTo(b))
        assertEquals("$a", "$b")
        println("$a == $b")
    }
}
