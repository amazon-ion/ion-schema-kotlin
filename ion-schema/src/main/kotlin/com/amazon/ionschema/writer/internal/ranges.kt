// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal

import com.amazon.ion.IonWriter
import com.amazon.ionschema.model.DiscreteIntRange

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
