package com.amazon.ionschema.model

import com.amazon.ion.IonValue

/**
 * Represents an argument to the `valid_values` constraint.
 *
 * Consumers of `ion-schema-kotlin` MAY NOT implement this interface.
 *
 * @see [Constraint.ValidValues]
 */
// TODO: Make "sealed" when updating to kotlin 1.5 or higher.
interface ValidValue {
    /**
     * A single Ion value. May not be annotated.
     * Ignoring annotations, this value is compared using Ion equivalence with data that is being validated.
     * @see [Constraint.ValidValues]
     */
    data class Value(val value: IonValue) : ValidValue {
        init { require(value.typeAnnotations.isEmpty()) { "valid value may not be annotated" } }
    }

    /**
     * A range over Ion numbers.
     */
    class NumberRange(start: Limit<ConsistentDecimal>, end: Limit<ConsistentDecimal>) : ValidValue, ContinuousRange<ConsistentDecimal>(start, end) {
        private constructor(value: Limit.Closed<ConsistentDecimal>) : this(value, value)
        constructor(value: ConsistentDecimal) : this(Limit.Closed(value))
    }

    /**
     * A range of timestamp values.
     */
    class TimestampRange(start: Limit<ConsistentTimestamp>, end: Limit<ConsistentTimestamp>) : ValidValue, ContinuousRange<ConsistentTimestamp>(start, end) {
        private constructor(value: Limit.Closed<ConsistentTimestamp>) : this(value, value)
        constructor(value: ConsistentTimestamp) : this(Limit.Closed(value))
    }
}
