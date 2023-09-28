// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal

import com.amazon.ion.IonType
import com.amazon.ion.IonValue
import com.amazon.ion.IonWriter

/**
 * Steps into a struct, writes [content] to this [IonWriter], and steps out of the struct in a `finally` block.
 */
internal inline fun IonWriter.writeStruct(content: IonWriter.() -> Unit) {
    try {
        stepIn(IonType.STRUCT)
        content()
    } finally {
        stepOut()
    }
}

/**
 * Steps into a struct, invokes [valueWriter] for each element of [values], and steps out of the struct in a `finally` block.
 */
internal inline fun <T> IonWriter.writeToStruct(values: Map<String, T>, valueWriter: IonWriter.(T) -> Unit) {
    try {
        stepIn(IonType.STRUCT)
        values.forEach { (k, v) ->
            setFieldName(k)
            valueWriter(v)
        }
    } finally {
        stepOut()
    }
}

/**
 * Steps into a list, writes [content] to this [IonWriter], and steps out of the list in a `finally` block.
 */
internal inline fun IonWriter.writeList(content: IonWriter.() -> Unit) {
    try {
        stepIn(IonType.LIST)
        content()
    } finally {
        stepOut()
    }
}

/**
 * Steps into a list, invokes [valueWriter] for each element of [values], and steps out of the list in a `finally` block.
 */
internal inline fun <T> IonWriter.writeToList(values: Iterable<T>, valueWriter: IonWriter.(T) -> Unit) {
    try {
        stepIn(IonType.LIST)
        values.forEach { valueWriter(it) }
    } finally {
        stepOut()
    }
}

/**
 * Writes an [IonValue] to an [IonWriter].
 */
internal fun IonWriter.writeIonValue(value: IonValue) = value.writeTo(this)
