package com.amazon.ionschema.reader.internal

import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.IonSchemaException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ReaderContextTest {
    companion object {
        private val ION = IonSystemBuilder.standard().build()
    }

    @Test
    fun `reportError() should throw when failFast is true`() {
        val context = ReaderContext(failFast = true)
        assertThrows<IonSchemaException> {
            context.reportError(ReadError(ION.singleValue("-1"), "Warp drive setting must be positive."))
        }
    }

    @Test
    fun `reportError() should not throw when failFast is false`() {
        val context = ReaderContext(failFast = false)
        val error1 = ReadError(ION.singleValue("-1"), "Warp drive setting must be positive.")
        val error2 = ReadError(ION.singleValue("11"), "Warp drive setting must be less than 10.")
        context.reportError(error1)
        context.reportError(error2)
        assertEquals(listOf(error1, error2), context.readErrors)
    }
}
