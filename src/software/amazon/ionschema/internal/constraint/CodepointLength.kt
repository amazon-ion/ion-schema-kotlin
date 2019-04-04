package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonText
import software.amazon.ion.IonValue

/**
 * Implements the codepoint_length constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#codepoint_length
 */
internal class CodepointLength(
        ion: IonValue
) : ConstraintBaseIntRange<IonText>(IonText::class.java, ion) {

    override val violationCode = "invalid_codepoint_length"
    override val violationMessage = "invalid codepoint length %s, expected %s"

    override fun getIntValue(value: IonText) = value.stringValue().length
}

