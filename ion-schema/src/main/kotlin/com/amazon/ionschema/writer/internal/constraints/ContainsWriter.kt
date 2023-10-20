// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ion.IonWriter
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.writer.internal.writeIonValue
import com.amazon.ionschema.writer.internal.writeToList

@ExperimentalIonSchemaModel
internal object ContainsWriter : ConstraintWriter {
    override val supportedClasses = setOf(Constraint.Contains::class)

    override fun IonWriter.write(c: Constraint) {
        check(c is Constraint.Contains)
        setFieldName("contains")
        writeToList(c.values) { writeIonValue(it) }
    }
}
