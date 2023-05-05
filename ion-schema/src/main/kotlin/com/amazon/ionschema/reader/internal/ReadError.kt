package com.amazon.ionschema.reader.internal

import com.amazon.ion.IonValue
import com.amazon.ionschema.reader.IonTreePath.Companion.treePath

/**
 * Represents an error that occurred while attempting to read Ion as an Ion Schema value.
 * The [value] is the IonValue where the error was detected, and [message] is a description of the problem.
 */
data class ReadError(val value: IonValue, val message: String) {
    internal val path by lazy { value.treePath() }
}
