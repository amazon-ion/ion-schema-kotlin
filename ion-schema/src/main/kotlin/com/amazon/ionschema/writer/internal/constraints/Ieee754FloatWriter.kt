// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ion.IonWriter
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel

@ExperimentalIonSchemaModel
object Ieee754FloatWriter : ConstraintWriter {
    override val supportedClasses = setOf(Constraint.Ieee754Float::class)

    override fun IonWriter.write(c: Constraint) {
        check(c is Constraint.Ieee754Float)
        setFieldName("ieee754_float")
        writeSymbol(c.format.symbolText)
    }
}
