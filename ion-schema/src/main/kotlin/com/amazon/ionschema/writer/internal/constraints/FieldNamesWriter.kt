// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ion.IonWriter
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.writer.internal.TypeWriter

@ExperimentalIonSchemaModel
internal class FieldNamesWriter(private val typeWriter: TypeWriter) : ConstraintWriter {
    override val supportedClasses = setOf(Constraint.FieldNames::class)

    override fun IonWriter.write(c: Constraint) {
        check(c is Constraint.FieldNames)
        setFieldName("field_names")
        if (c.distinct) {
            setTypeAnnotations("distinct")
        }
        typeWriter.writeTypeArg(this@write, c.type)
    }
}
