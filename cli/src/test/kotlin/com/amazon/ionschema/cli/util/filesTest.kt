package com.amazon.ionschema.cli.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DetectIndentTest {
    @Test
    fun `when no indentation should return null`() {
        val text = """
            |Lorem ipsum dolor englebert.
            |
            |Quando omni flunkus moritati.
        """.trimMargin()
        Assertions.assertEquals(null, text.inferIndent())
    }

    @Test
    fun `when spaces indentation should return correct number of spaces`() {
        val text = """
            |type::{
            |  name: positive_int,
            |  type: int,
            |  valid_values: range::[1, max],
            |}
        """.trimMargin()
        Assertions.assertEquals("  ", text.inferIndent())
    }

    @Test
    fun `when spaces indentation is inconsistent should return null`() {
        val text = """
            |type::{
            |    name: positive_int,
            |   type: int,
            |     valid_values: range::[1, max],
            |}
        """.trimMargin()
        Assertions.assertEquals(null, text.inferIndent())
    }

    @Test
    fun `when any tabs should return tab character`() {
        val t = '\t'
        val text = """
            |class Foo {
            |${t}val bar = 1
            |}
        """.trimMargin()
        Assertions.assertEquals("\t", text.inferIndent())
    }

    @Test
    fun `blank lines should not influence the outcome`() {
        val tab = '\t'
        val spaces = "     "
        val text = """
            |type::{
            |
            |  name: positive_int,
            | $spaces
            |  type: int,
            |$tab$tab$tab
            |  valid_values: range::[1, max],
            |}
        """.trimMargin()
        Assertions.assertEquals("  ", text.inferIndent())
    }
}
