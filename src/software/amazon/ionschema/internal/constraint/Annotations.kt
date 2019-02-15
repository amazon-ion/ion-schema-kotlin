package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonList
import software.amazon.ion.IonSymbol
import software.amazon.ion.IonValue
import software.amazon.ionschema.Constraint
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation
import software.amazon.ionschema.internal.util.withoutTypeAnnotations

internal class Annotations private constructor(
        ion: IonValue,
        private val delegate: Constraint
) : ConstraintBase(ion), Constraint by delegate {

    constructor(ion: IonValue) : this(ion, delegate(ion))

    companion object {
        private fun delegate(ion: IonValue): Constraint {
            val requiredByDefault = ion.hasTypeAnnotation("required")
            val annotations = (ion as IonList).map {
                Annotation(it as IonSymbol, requiredByDefault)
            }
            return if (ion.hasTypeAnnotation("ordered")) {
                    OrderedAnnotations(ion, annotations)
                } else {
                    UnorderedAnnotations(ion, annotations)
                }
        }
    }

    override fun name() = delegate.name()
}

internal class OrderedAnnotations(
        ion: IonValue,
        private val annotations: List<Annotation>
) : ConstraintBase(ion) {

    private val ION = ion.system

    private val stateMachine: StateMachine

    init {
        val stateMachineBuilder = StateMachineBuilder()

        // support for open content at the beginning:
        stateMachineBuilder.addTransition(
                stateMachineBuilder.initialState, Event.ANY, stateMachineBuilder.initialState)

        var state = stateMachineBuilder.initialState

        (ion as IonList).forEachIndexed { idx, it ->
            val newState = State(isFinal = idx == ion.size - 1)
            val annotationSymbol = it.withoutTypeAnnotations()
            stateMachineBuilder.addTransition(state, Event(annotationSymbol), newState)
            if (!annotations[idx].isRequired) {
                // optional annotations are modeled as no-op events
                stateMachineBuilder.addTransition(state, Event.NOOP, newState)
            }

            // support for open content
            stateMachineBuilder.addTransition(newState, Event.ANY, newState)

            state = newState
        }

        stateMachine = stateMachineBuilder.build()
    }

    override fun validate(value: IonValue, issues: Violations) {
        if (!stateMachine.matches(value.typeAnnotations.map { ION.newSymbol(it) }.iterator())) {
            issues.add(Violation(ion, "annotations_mismatch", "annotations don't match expectations"))
        }
    }
}

internal class UnorderedAnnotations(
        ion: IonValue,
        private val annotations: List<Annotation>
) : ConstraintBase(ion) {

    override fun validate(value: IonValue, issues: Violations) {
        val missingAnnotations = mutableListOf<Annotation>()
        annotations.forEach {
            if (it.isRequired && !value.hasTypeAnnotation(it.text)) {
                missingAnnotations.add(it)
            }
        }

        if (missingAnnotations.size > 0) {
            issues.add(Violation(ion, "missing_annotation",
                    "missing annotation(s): " + missingAnnotations.joinToString { it.text }))
        }
    }
}

internal class Annotation(
        ion: IonSymbol,
        requiredByDefault: Boolean
) {
    val text = ion.stringValue()

    val isRequired = if (ion.hasTypeAnnotation("required")) {
            true
        } else if (ion.hasTypeAnnotation("optional")) {
            false
        } else {
            requiredByDefault
        }
}

