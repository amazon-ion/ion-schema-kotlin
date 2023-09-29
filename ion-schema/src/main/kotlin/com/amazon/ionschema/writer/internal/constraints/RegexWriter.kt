// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ion.IonWriter
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel

@ExperimentalIonSchemaModel
internal object RegexWriter : ConstraintWriter {
    override val supportedClasses = setOf(Constraint.Regex::class)

    override fun IonWriter.write(c: Constraint) {
        check(c is Constraint.Regex)
        setFieldName("regex")
        if (c.caseInsensitive) addTypeAnnotation("i")
        if (c.multiline) addTypeAnnotation("m")
        writeString(c.pattern)
    }
}
