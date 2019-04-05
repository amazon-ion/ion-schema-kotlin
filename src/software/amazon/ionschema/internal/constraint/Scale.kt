package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonDecimal
import software.amazon.ion.IonValue

/**
 * Implements the scale constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#scale
 */
internal class Scale(
        ion: IonValue
) : ConstraintBaseIntRange<IonDecimal>(IonDecimal::class.java, ion) {

    override val violationCode = "invalid_scale"
    override val violationMessage = "invalid scale %s, expected %s"

    override fun getIntValue(value: IonDecimal) = value.bigDecimalValue().scale()
}

