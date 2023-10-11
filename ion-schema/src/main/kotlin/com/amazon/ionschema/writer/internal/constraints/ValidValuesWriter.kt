// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ion.IonWriter
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.ValidValue
import com.amazon.ionschema.writer.internal.writeIonValue
import com.amazon.ionschema.writer.internal.writeNumberRange
import com.amazon.ionschema.writer.internal.writeTimestampRange
import com.amazon.ionschema.writer.internal.writeToList

@ExperimentalIonSchemaModel
internal object ValidValuesWriter : ConstraintWriter {
    override val supportedClasses = setOf(Constraint.ValidValues::class)

    override fun IonWriter.write(c: Constraint) {
        check(c is Constraint.ValidValues)
        setFieldName("valid_values")
        writeToList(c.values) {
            when (it) {
                is ValidValue.NumberRange -> writeNumberRange(it)
                is ValidValue.TimestampRange -> writeTimestampRange(it)
                is ValidValue.Value -> writeIonValue(it.value)
            }
        }
    }
}
