// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ion.IonWriter
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.writer.internal.writeRange

@ExperimentalIonSchemaModel
internal object ExponentWriter : ConstraintWriter {
    override val supportedClasses = setOf(Constraint.Exponent::class)
    override fun IonWriter.write(c: Constraint) {
        check(c is Constraint.Exponent)
        setFieldName("exponent")
        writeRange(c.range)
    }
}
