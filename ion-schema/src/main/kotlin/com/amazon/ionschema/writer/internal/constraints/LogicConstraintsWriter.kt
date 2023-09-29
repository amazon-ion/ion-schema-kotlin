// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ion.IonWriter
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.writer.internal.TypeWriter
import com.amazon.ionschema.writer.internal.writeTypeArguments
import kotlin.reflect.KClass

@ExperimentalIonSchemaModel
internal class LogicConstraintsWriter(private val typeWriter: TypeWriter) : ConstraintWriter {
    override val supportedClasses: Set<KClass<out Constraint>> = setOf(
        Constraint.AllOf::class,
        Constraint.AnyOf::class,
        Constraint.Not::class,
        Constraint.OneOf::class,
        Constraint.Type::class,
    )

    override fun IonWriter.write(c: Constraint) {
        when (c) {
            is Constraint.AllOf -> {
                setFieldName("all_of")
                typeWriter.writeTypeArguments(this@write, c.types)
            }
            is Constraint.AnyOf -> {
                setFieldName("any_of")
                typeWriter.writeTypeArguments(this@write, c.types)
            }
            is Constraint.OneOf -> {
                setFieldName("one_of")
                typeWriter.writeTypeArguments(this@write, c.types)
            }
            is Constraint.Not -> {
                setFieldName("not")
                typeWriter.writeTypeArg(this@write, c.type)
            }
            is Constraint.Type -> {
                setFieldName("type")
                typeWriter.writeTypeArg(this@write, c.type)
            }
            else -> check(false)
        }
    }
}
