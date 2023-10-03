// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ion.IonWriter
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import kotlin.reflect.KClass

/**
 * Allows us to compose TypeWriters out of different combinations of constraint writers to enable code reuse across
 * multiple Ion Schema versions.
 */
@ExperimentalIonSchemaModel
internal interface ConstraintWriter {
    /**
     * Returns the constraint types that can be written by this constraint writer.
     */
    val supportedClasses: Set<KClass<out Constraint>>

    /**
     * Writes a [Constraint] instance to the given IonWriter.
     * Must throw [IllegalStateException] if called for an unsupported constraint type.
     * Equivalent to [write], but more convenient for callers.
     */
    fun writeTo(ionWriter: IonWriter, constraint: Constraint) = ionWriter.write(constraint)

    /**
     * Writes a [Constraint] instance to the given IonWriter.
     * Must throw [IllegalStateException] if called for an unsupported constraint type.
     * Equivalent to [writeTo], but more convenient for implementers.
     */
    fun IonWriter.write(c: Constraint)
}
