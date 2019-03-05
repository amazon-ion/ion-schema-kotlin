package software.amazon.ionschema.internal

import software.amazon.ion.IonValue
import software.amazon.ionschema.Violation

/**
 * Provides methods to create Violations that are common across multiple
 * constraints.
 */
class CommonViolations private constructor() {
    companion object {
        @JvmStatic
        fun INVALID_TYPE(constraint: IonValue, value: IonValue) = Violation(
                constraint,
                "invalid_type",
                "not applicable for type %s".format(value.type.toString().toLowerCase())
        )

        @JvmStatic
        fun NULL_VALUE(constraint: IonValue) = Violation(
                constraint,
                "null_value",
                "not applicable for null values"
        )
    }
}

