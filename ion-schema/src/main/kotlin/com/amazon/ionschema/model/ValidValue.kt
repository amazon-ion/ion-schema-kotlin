package com.amazon.ionschema.model

import com.amazon.ion.IonValue

/**
 * Represents an argument to the `valid_values` constraint.
 * @see [Constraint.ValidValues]
 */
sealed class ValidValue {
    /**
     * A single Ion value. May not be annotated.
     * Ignoring annotations, this value is compared using Ion equivalence with data that is being validated.
     * @see [Constraint.ValidValues]
     */
    class Value(val value: IonValue) : ValidValue() {
        init { require(value.typeAnnotations.isEmpty()) { "valid value may not be annotated" } }
    }

    /**
     * A range of numbers.
     */
    class IonNumberRange(val range: NumberRange) : ValidValue()

    /**
     * A range of timestamp values.
     */
    class IonTimestampRange(val range: TimestampRange) : ValidValue()
}
