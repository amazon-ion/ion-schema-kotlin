// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal

import com.amazon.ion.IonWriter
import com.amazon.ionschema.model.ContinuousRange
import com.amazon.ionschema.model.DiscreteIntRange
import com.amazon.ionschema.model.TimestampPrecisionRange
import com.amazon.ionschema.model.ValidValue

internal fun IonWriter.writeRange(range: DiscreteIntRange) {
    val (start, endInclusive) = range
    if (start == endInclusive) {
        writeInt(start!!.toLong())
    } else {
        setTypeAnnotations("range")
        writeList {
            start?.let { writeInt(it.toLong()) }
                ?: writeSymbol("min")
            endInclusive?.let { writeInt(it.toLong()) }
                ?: writeSymbol("max")
        }
    }
}

internal fun IonWriter.writeNumberRange(range: ValidValue.NumberRange) = write(range) { writeDecimal(it.bigDecimalValue) }
internal fun IonWriter.writeTimestampRange(range: ValidValue.TimestampRange) = write(range) { writeTimestamp(it.timestampValue) }
internal fun IonWriter.writeTimestampPrecisionRange(range: TimestampPrecisionRange) = write(range, elideSingletons = true) { writeSymbol(it.toSymbolTextOrNull()!!) }

private inline fun <T : Comparable<T>> IonWriter.write(range: ContinuousRange<T>, elideSingletons: Boolean = false, writeValue: IonWriter.(T) -> Unit) {
    if (elideSingletons && range.start is ContinuousRange.Limit.Closed && range.start == range.end) {
        writeValue(range.start.value)
        return
    }
    setTypeAnnotations("range")
    writeList {
        when (val start = range.start) {
            is ContinuousRange.Limit.Unbounded -> writeSymbol("min")
            is ContinuousRange.Limit.Closed -> writeValue(start.value)
            is ContinuousRange.Limit.Open -> {
                setTypeAnnotations("exclusive")
                writeValue(start.value)
            }
        }
        when (val end = range.end) {
            is ContinuousRange.Limit.Unbounded -> writeSymbol("max")
            is ContinuousRange.Limit.Closed -> writeValue(end.value)
            is ContinuousRange.Limit.Open -> {
                setTypeAnnotations("exclusive")
                writeValue(end.value)
            }
        }
    }
}
