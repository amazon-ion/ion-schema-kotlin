// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ion.IonWriter
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.VariablyOccurringTypeArgument.Companion.OCCURS_OPTIONAL
import com.amazon.ionschema.writer.internal.TypeWriter
import com.amazon.ionschema.writer.internal.writeToStruct

@ExperimentalIonSchemaModel
internal class FieldsWriter(private val typeWriter: TypeWriter, private val ionSchemaVersion: IonSchemaVersion) : ConstraintWriter {
    override val supportedClasses = setOf(Constraint.Fields::class)

    override fun IonWriter.write(c: Constraint) {
        check(c is Constraint.Fields)

        if (c.closed && ionSchemaVersion == IonSchemaVersion.v1_0) {
            setFieldName("content")
            writeSymbol("closed")
        }

        setFieldName("fields")
        if (c.closed && ionSchemaVersion != IonSchemaVersion.v1_0) setTypeAnnotations("closed")
        writeToStruct(c.fields) {
            typeWriter.writeVariablyOccurringTypeArg(this@writeToStruct, it, elideOccursValue = OCCURS_OPTIONAL)
        }
    }
}
