package com.amazon.ionschema.model

import com.amazon.ion.Timestamp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConsistentTimestampTest {

    @Test
    fun `compareTo, hashCode, and equals are consistent when precision is different`() {
        val a = ConsistentTimestamp(Timestamp.valueOf("2022-01-01T00:00Z"))
        val b = ConsistentTimestamp(Timestamp.valueOf("2022-01-01T00:00:00.000Z"))

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals(0, a.compareTo(b))
        println("$a == $b")
    }

    @Test
    fun `compareTo, hashCode, and equals are consistent when offset is different`() {
        val a = ConsistentTimestamp(Timestamp.valueOf("2022-01-01T04:30:00+04:30"))
        val b = ConsistentTimestamp(Timestamp.valueOf("2022-01-01T00:00:00Z"))

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals(0, a.compareTo(b))
        println("$a == $b")
    }
}
