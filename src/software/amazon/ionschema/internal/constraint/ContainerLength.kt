package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonContainer
import software.amazon.ion.IonValue

/**
 * Implements the container_length constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#container_length
 */
internal class ContainerLength(
        ion: IonValue
) : ConstraintBaseIntRange<IonContainer>(IonContainer::class.java, ion) {

    override val violationCode = "invalid_container_length"
    override val violationMessage = "invalid container length %s, expected %s"

    override fun getIntValue(value: IonContainer) = value.size()
}

