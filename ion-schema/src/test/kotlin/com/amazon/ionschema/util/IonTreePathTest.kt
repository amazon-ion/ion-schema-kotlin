package com.amazon.ionschema.util

import com.amazon.ion.IonList
import com.amazon.ion.IonSexp
import com.amazon.ion.IonStruct
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.reader.IonTreePath
import com.amazon.ionschema.reader.IonTreePath.Companion.treePath
import com.amazon.ionschema.reader.IonTreePath.Element
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class IonTreePathTest {
    companion object {
        private val ION = IonSystemBuilder.standard().build()
    }

    @Test
    fun `treePath() - root is sequence`() {
        val list = ION.singleValue(
            """
          [
            foo, bar, baz,
            [
              0, 1,
              { 
                a: { b: (c d e) } 
              }
            ]
          ]
        """
        )
        val value = (((((list as IonList)[3] as IonList)[2] as IonStruct)["a"] as IonStruct)["b"] as IonSexp)[1]

        val path = value.treePath()
        val expectedPath = IonTreePath(
            listOf(
                Element.Index(3),
                Element.Index(2),
                Element.Field("a"),
                Element.Field("b"),
                Element.Index(1),
            )
        )
        assertEquals(expectedPath, path)
    }

    @Test
    fun `treePath() - root is struct`() {
        val dg = ION.singleValue(" { a: { b: (c d e) } } ")
        val dSymbol = (((dg as IonStruct)["a"] as IonStruct)["b"] as IonSexp)[1]
        val path = dSymbol.treePath()
        val expectedPath = IonTreePath(
            listOf(
                Element.Field("a"),
                Element.Field("b"),
                Element.Index(1),
            )
        )
        assertEquals(expectedPath, path)
    }

    @Test
    fun `treePath() - root is scalar`() {
        val value = ION.singleValue(" false ")
        val path = value.treePath()
        val expectedPath = IonTreePath(emptyList())
        assertEquals(expectedPath, path)
    }

    @Test
    fun `treePath() - path length of one`() {
        val dg = ION.loader.load(" foo bar baz ")
        val path = dg[1].treePath()
        val expectedPath = IonTreePath(listOf(Element.Index(1)))
        assertEquals(expectedPath, path)
    }

    @Test
    fun `treePath() - empty path`() {
        val ion = ION.singleValue("{}")
        assertEquals(IonTreePath(emptyList()), ion.treePath())
    }

    @Test
    fun `compareTo() - indices before fields`() {
        val path1 = IonTreePath(listOf(Element.Field("foo"), Element.Index(1)))
        val path2 = IonTreePath(listOf(Element.Field("foo"), Element.Field("1")))
        assertTrue(path1 < path2)
    }

    @Test
    fun `compareTo() - fields in string order`() {
        val path1 = IonTreePath(listOf(Element.Field("a")))
        val path2 = IonTreePath(listOf(Element.Field("b")))
        assertTrue(path1 < path2)
    }

    @Test
    fun `compareTo() - indices in numerical order`() {
        val path1 = IonTreePath(listOf(Element.Index(1)))
        val path2 = IonTreePath(listOf(Element.Index(2)))
        assertTrue(path1 < path2)
    }

    @Test
    fun `compareTo() - shorter is less than longer`() {
        val path1 = IonTreePath(listOf())
        val path2 = IonTreePath(listOf(Element.Index(1)))
        assertTrue(path1 < path2)
    }

    @Test
    fun `compareTo() - equal paths are equal`() {
        val path1 = IonTreePath(listOf(Element.Field("foo"), Element.Index(1)))
        val path2 = IonTreePath(listOf(Element.Field("foo"), Element.Index(1)))
        assertFalse(path1 < path2)
        assertFalse(path1 > path2)
    }
}
