// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalIonSchemaModel::class)

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ion.IonWriter
import com.amazon.ionschema.assertEqualIon
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.writer.internal.writeStruct
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

/**
 * Base class for eliminating boilerplate code in the constraint writer test classes.
 *
 * Given a [ConstraintWriter] instance, this will check:
 * 1. That [ConstraintWriter.supportedClasses] returns the correct values
 * 2. That [ConstraintWriter.writeTo] throws if called for the wrong constraint type
 * 3. That [ConstraintWriter.writeTo] writes the expected field name and value to a struct
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class ConstraintTestBase internal constructor(
    internal val writer: ConstraintWriter,
    protected val expectedConstraints: Set<KClass<out Constraint>>,
    /**
     * Pairs of [Constraint] instances to the field name and value that is expected for writing that constraint. E.g.:
     * ```
     * listOf(
     *     Constraint.Exponent(DiscreteIntRange(null, 23)) to "exponent: range::[min, 23]",
     *     Constraint.Exponent(DiscreteIntRange(7, null)) to "exponent: range::[7, max]",
     * )
     * ```
     */
    protected val writeTestCases: List<Pair<Constraint, String>>
) {
    /** Runs the test cases given in [writeTestCases]. */
    @ParameterizedTest
    @MethodSource("getWriteTestCases")
    private fun `writer should be able to write constraint`(testCase: Pair<Constraint, String>) = runWriteCase(writer, testCase)

    /** Helper function that can be used by subclasses to run additional "write" test cases */
    internal fun runWriteCase(writer: ConstraintWriter, testCase: Pair<Constraint, String>) {
        val (constraint, expectedField) = testCase
        assertEqualIon("{ $expectedField }") {
            it.writeStruct {
                writer.writeTo(it, constraint)
            }
        }
    }

    @Test
    private fun `supportedClasses should return the correct classes`() {
        assertEquals(expectedConstraints, writer.supportedClasses)
    }

    @Test
    private fun `attempting to write an unsupported constraint should throw an exception`() {
        val ionWriter = mockk<IonWriter>()
        val constraint = mockk<Constraint>()
        assertThrows<IllegalStateException> {
            writer.writeTo(ionWriter, constraint)
        }
    }
}
