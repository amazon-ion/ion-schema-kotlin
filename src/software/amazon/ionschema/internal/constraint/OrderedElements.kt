package software.amazon.ionschema.internal.constraint

import software.amazon.ion.*
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.Schema
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation
import software.amazon.ionschema.internal.TypeReference
import software.amazon.ionschema.internal.util.IntRange

/**
 * Implements the ordered_element constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#ordered_elements
 */
internal class OrderedElements(
        ion: IonValue,
        private val schema: Schema
) : ConstraintBase(ion) {

    private val stateMachine: StateMachine

    init {
        if (ion !is IonList || ion.isNullValue) {
            throw InvalidSchemaException("Invalid ordered_elements constraint: $ion")
        }

        val stateMachineBuilder = StateMachineBuilder()

        var state: State? = null
        ion.forEachIndexed { idx, it ->
            val occursRange = IntRange.toIntRange((it as? IonStruct)?.get("occurs")) ?: IntRange.REQUIRED
            val newState = State(occursRange, isFinal = idx == ion.size - 1)
            val typeResolver = TypeReference.create(it, schema)
            stateMachineBuilder.addTransition(state, EventSchemaType(typeResolver), newState)
            state = newState
        }

        stateMachine = stateMachineBuilder.build()
    }

    override fun validate(value: IonValue, issues: Violations) {
        validateAs<IonSequence>(value, issues) { v ->
            if (!stateMachine.matches(v.iterator())) {
                issues.add(Violation(ion, "ordered_elements_mismatch",
                        "one or more ordered elements don't match specification"))
            }
        }
    }
}

