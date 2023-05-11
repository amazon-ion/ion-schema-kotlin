package com.amazon.ionschema.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class BagTest {

    @Test
    fun `A Bag should not be equal to any other type of collection`() {
        val bag = bagOf(1, 2, 3)
        val set = setOf(1, 2, 3, 4)
        assertNotEquals(bag, set)
    }

    @Test
    fun `Bags with different content should not be equal`() {
        val bag1 = bagOf(1, 2, 3)
        val bag2 = bagOf(1, 2, 3, 4)
        assertNotEquals(bag1, bag2)
    }

    @Test
    fun `Bags with different quantities of the same content should not be equal`() {
        val bag1 = bagOf(1, 2, 3)
        val bag2 = bagOf(1, 1, 2, 2, 3, 3)
        assertNotEquals(bag1, bag2)
    }

    @Test
    fun `Bags with the same content in supposedly different order should be equal`() {
        val bag1 = bagOf(1, 2, 3)
        val bag2 = bagOf(3, 2, 1)
        assertEquals(bag1, bag2)
    }

    @Test
    fun `empty Bags should be equal`() {
        val bag1 = Bag<Nothing>()
        val bag2 = Bag<Nothing>()
        assertEquals(bag1, bag2)
    }

    @Test
    fun `equal Bags should have the equal hashcodes`() {
        assertEquals(Bag<Nothing>().hashCode(), Bag<Nothing>().hashCode())
        assertEquals(bagOf(1).hashCode(), bagOf(1).hashCode())
        assertEquals(bagOf(1, 2, 3).hashCode(), bagOf(3, 2, 1).hashCode())
    }

    @Test
    fun `emptyBag() should return a singleton object`() {
        val bag1 = emptyBag<String>()
        val bag2 = emptyBag<Nothing>()
        assertSame(bag1, bag2)
    }

    @Test
    fun `bagOf() with no elements should return the singleton emptyBag object`() {
        assertSame(emptyBag<Nothing>(), bagOf<Nothing>())
    }
}
