// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ion.IonWriter
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel

/**
 * Allows us to compose TypeWriters out of different combinations of constraint writers to enable code reuse across
 * multiple Ion Schema versions.
 */
@ExperimentalIonSchemaModel
internal interface ConstraintWriter {
    /**
     * Returns true if this constraint reader can read the given constraint.
     */
    fun canWrite(constraint: Constraint): Boolean

    /**
     * Writes a [Constraint] instance to the given IonWriter.
     * Should only be called after checking whether the constraint is supported by calling [canWrite].
     * Must throw [IllegalStateException] if called for an unsupported constraint type.
     */
    fun Constraint.writeTo(ionWriter: IonWriter)
}
