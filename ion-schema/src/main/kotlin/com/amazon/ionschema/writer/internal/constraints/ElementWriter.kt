// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ion.IonWriter
import com.amazon.ionschema.IonSchemaException
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.writer.internal.TypeWriter

@ExperimentalIonSchemaModel
internal class ElementWriter(private val typeWriter: TypeWriter, private val ionSchemaVersion: IonSchemaVersion) : ConstraintWriter {
    override val supportedClasses = setOf(Constraint.Element::class)

    override fun IonWriter.write(c: Constraint) {
        check(c is Constraint.Element)

        setFieldName("element")
        if (c.distinct) {
            if (ionSchemaVersion == IonSchemaVersion.v1_0) {
                throw IonSchemaException("Ion Schema 1.0 does not support 'distinct' elements")
            } else {
                setTypeAnnotations("distinct")
            }
        }
        typeWriter.writeTypeArg(this@write, c.type)
    }
}
