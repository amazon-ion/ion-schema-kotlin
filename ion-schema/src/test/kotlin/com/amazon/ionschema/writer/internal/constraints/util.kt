// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ion.IonWriter
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.TypeArgument
import com.amazon.ionschema.model.optional
import com.amazon.ionschema.model.required
import com.amazon.ionschema.writer.internal.TypeWriter
import io.mockk.every
import io.mockk.mockk

private val ION = IonSystemBuilder.standard().build()

/** Helper fun for creating IonValue instances */
fun ion(text: String) = ION.singleValue(text)

/**
 * Creates a mock TypeWriter that can write the given type names.
 * This is a mock, though, and it's functionality is not complete. Does not write nullability annotations.
 * Does not respect the "occurs" value of any [VariablyOccurringTypeArgument]s.
 */
@OptIn(ExperimentalIonSchemaModel::class)
internal fun stubTypeWriterWithRefs(vararg refs: String) = mockk<TypeWriter> {
    refs.forEach { ref ->
        every { writeTypeArg(any(), TypeArgument.Reference(ref)) } answers {
            (it.invocation.args[0] as IonWriter).writeSymbol(ref)
        }
        every { writeVariablyOccurringTypeArg(any(), TypeArgument.Reference(ref).optional(), any()) } answers {
            (it.invocation.args[0] as IonWriter).writeSymbol(ref)
        }
        every { writeVariablyOccurringTypeArg(any(), TypeArgument.Reference(ref).required(), any()) } answers {
            (it.invocation.args[0] as IonWriter).writeSymbol(ref)
        }
    }
}
