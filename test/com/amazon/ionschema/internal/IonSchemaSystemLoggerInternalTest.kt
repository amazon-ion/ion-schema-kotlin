package com.amazon.ionschema.internal

import com.amazon.ionschema.IonSchemaSystemLogger
import com.amazon.ionschema.LogLevel
import org.junit.Test
import kotlin.test.assertEquals

class IonSchemaSystemLoggerInternalTest {

    @Test
    fun testInternalWrapper() {
        // Tests both the delegation to the callback and the prepending of the
        // [ion-schema-kotlin] tag to the log message.

        val logEntries = mutableListOf<Pair<LogLevel, String>>()
        val loggerCallback: IonSchemaSystemLogger =
            { level, message -> logEntries.add(level to message()) }
        val internalLogger = IonSchemaSystemLoggerInternal(loggerCallback)

        internalLogger.invoke(LogLevel.Info) { "This is an info message." }
        internalLogger.invoke(LogLevel.Warn) { "This is a warning message." }

        assertEquals(2, logEntries.size, message = "Wrong number of log entries were created.")
        logEntries[0].let { (level, msg) ->
            assertEquals(LogLevel.Info, level)
            assertEquals("[ion-schema-kotlin] This is an info message.", msg)
        }
        logEntries[1].let { (level, msg) ->
            assertEquals(LogLevel.Warn, level)
            assertEquals("[ion-schema-kotlin] This is a warning message.", msg)
        }
    }
}
