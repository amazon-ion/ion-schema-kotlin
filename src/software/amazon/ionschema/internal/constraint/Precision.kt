package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonDecimal
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException

/**
 * Implements the precision constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#precision
 */
internal class Precision(
        ion: IonValue
) : ConstraintBaseIntRange<IonDecimal>(IonDecimal::class.java, ion) {

    init {
        if (range.contains(0)) {
            throw InvalidSchemaException("Precision must be at least 1 ($ion)")
        }
    }

    override val violationCode = "invalid_precision"
    override val violationMessage = "invalid precision %s, expected %s"

    override fun getIntValue(value: IonDecimal) = value.bigDecimalValue().precision()
}

