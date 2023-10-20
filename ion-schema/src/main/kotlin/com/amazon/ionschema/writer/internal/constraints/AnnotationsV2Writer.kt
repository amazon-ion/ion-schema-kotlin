// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ion.IonWriter
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.Constraint.AnnotationsV2.Modifier.Closed
import com.amazon.ionschema.model.Constraint.AnnotationsV2.Modifier.Exact
import com.amazon.ionschema.model.Constraint.AnnotationsV2.Modifier.Required
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.writer.internal.TypeWriter
import com.amazon.ionschema.writer.internal.writeToList

@ExperimentalIonSchemaModel
internal class AnnotationsV2Writer(private val typeWriter: TypeWriter) : ConstraintWriter {

    override val supportedClasses = setOf(
        Constraint.AnnotationsV2.Simplified::class,
        Constraint.AnnotationsV2.Standard::class
    )

    override fun IonWriter.write(c: Constraint) {
        check(c is Constraint.AnnotationsV2)

        setFieldName("annotations")
        when (c) {
            is Constraint.AnnotationsV2.Standard -> typeWriter.writeTypeArg(this, c.type)
            is Constraint.AnnotationsV2.Simplified -> {
                when (c.modifier) {
                    Closed -> setTypeAnnotations("closed")
                    Required -> setTypeAnnotations("required")
                    Exact -> setTypeAnnotations("closed", "required")
                }
                writeToList(c.annotations) { writeSymbol(it) }
            }
        }
    }
}
