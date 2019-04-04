package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonLob
import software.amazon.ion.IonValue

/**
 * Implements the byte_length constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#byte_length
 */
internal class ByteLength(
        ion: IonValue
) : ConstraintBaseIntRange<IonLob>(IonLob::class.java, ion) {

    override val violationCode = "invalid_byte_length"
    override val violationMessage = "invalid byte length %s, expected %s"

    override fun getIntValue(value: IonLob) = value.byteSize()
}

