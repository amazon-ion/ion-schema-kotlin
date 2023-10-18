// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ion.IonWriter
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.VariablyOccurringTypeArgument.Companion.OCCURS_REQUIRED
import com.amazon.ionschema.writer.internal.TypeWriter
import com.amazon.ionschema.writer.internal.writeToList

@ExperimentalIonSchemaModel
internal class OrderedElementsWriter(private val typeWriter: TypeWriter) : ConstraintWriter {
    override val supportedClasses = setOf(Constraint.OrderedElements::class)

    override fun IonWriter.write(c: Constraint) {
        check(c is Constraint.OrderedElements)
        setFieldName("ordered_elements")
        writeToList(c.types) {
            typeWriter.writeVariablyOccurringTypeArg(this, it, elideOccursValue = OCCURS_REQUIRED)
        }
    }
}
