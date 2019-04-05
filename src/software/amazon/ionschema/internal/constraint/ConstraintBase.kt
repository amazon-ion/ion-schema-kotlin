package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonValue
import software.amazon.ionschema.Violations
import software.amazon.ionschema.internal.CommonViolations
import software.amazon.ionschema.internal.Constraint

/**
 * Base class for constraint implementations.
 */
internal abstract class ConstraintBase(
        val ion: IonValue
) : Constraint {

    override val name = ion.fieldName

    internal inline fun <reified T> validateAs(value: IonValue, issues: Violations, noinline customValidation: (T) -> Unit)
            = validateAs(T::class.java, value, issues, customValidation)

    internal fun <T> validateAs(expectedClass: Class<T>, value: IonValue, issues: Violations, customValidation: (T) -> Unit) {
        when {
            !expectedClass.isInstance(value) -> issues.add(CommonViolations.INVALID_TYPE(ion, value))
            value.isNullValue -> issues.add(CommonViolations.NULL_VALUE(ion))
            else ->
                @Suppress("UNCHECKED_CAST")
                customValidation(value as T)
        }
    }

    override fun toString() = ion.toString()
}

