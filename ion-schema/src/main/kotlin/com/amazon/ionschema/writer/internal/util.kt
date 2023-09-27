// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal

import com.amazon.ion.IonType
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
