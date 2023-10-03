// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ion.IonWriter
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.writer.internal.writeRange
import kotlin.reflect.KClass

@ExperimentalIonSchemaModel
internal object LengthConstraintsWriter : ConstraintWriter {

    override val supportedClasses: Set<KClass<out Constraint>> = setOf(
        Constraint.ByteLength::class,
        Constraint.CodepointLength::class,
        Constraint.ContainerLength::class,
        Constraint.Utf8ByteLength::class,
    )

    override fun IonWriter.write(c: Constraint) {
        when (c) {
            is Constraint.ByteLength -> {
                setFieldName("byte_length")
                writeRange(c.range)
            }
            is Constraint.CodepointLength -> {
                setFieldName("codepoint_length")
                writeRange(c.range)
            }
            is Constraint.ContainerLength -> {
                setFieldName("container_length")
                writeRange(c.range)
            }
            is Constraint.Utf8ByteLength -> {
                setFieldName("utf8_byte_length")
                writeRange(c.range)
            }
            else -> throw IllegalStateException("Unsupported constraint. Should be unreachable.")
        }
    }
}
