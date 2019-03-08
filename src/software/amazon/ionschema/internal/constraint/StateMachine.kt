package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonValue

/**
 * Builder that provides a simple API for constructing a [StateMachine].
 */
internal class StateMachineBuilder {
    private val states = StateSet()
    val initialState = State()

    fun addTransition(fromState: State, event: Event, toState: State) {
        if (!states.contains(fromState)) {
            states.add(fromState)
        }
        fromState.transitions[event] = toState
    }

    fun build() = StateMachine(states, initialState)
}

/**
 * Implements a Non-deterministic Finite Automata (NFA) based on an algorithm developed
 * by Ken Thompson.  With thanks to Russ Cox for a very informative writeup.
 *
 * @see https://swtch.com/~rsc/regexp/regexp1.html
 * @see "Regular Expression Search Algorithm" by Ken Thompson
 *      Communications of the ACM 11(6) (June 1968), pp. 419–422
 *      http://doi.acm.org/10.1145/363347.363387
 */
internal class StateMachine(
        private val states: StateSet,
        private val initialState: State
) {
    fun matches(iter: Iterator<IonValue>): Boolean {
        var states = addNextStates(StateSet(initialState), initialState, Event.NOOP)

        iter.forEach {
            val newStates = StateSet()

            states.forEach { state ->
                addNextStates(newStates, state, Event(it))
                addNextStates(newStates, state, Event.ANY)
            }

            HashSet(newStates).forEach { newState ->
                addNextStates(newStates, newState, Event.NOOP)
            }

            states = newStates

            // short-circuit if we've arrived at a final state
            if (states.hasFinalState()) {
                return true
            }
        }

        return states.hasFinalState()
    }

    private fun addNextStates(states: StateSet, fromState: State, event: Event): StateSet {
        fromState.transitions.let {
            val toState = fromState.transitions[event]
            toState?.let {
                states.add(toState)
                if (event == Event.NOOP) {
                    // recursively add any no-op paths
                    addNextStates(states, toState, Event.NOOP)
                }
            }
        }
        return states
    }

    override fun toString(): String {
        val sb = StringBuilder()
        states.forEach {
            sb.appendln(it)
            it.transitions.forEach {
                sb.appendln("  ${it.key} --> ${it.value}")
            }
        }
        return sb.toString()
    }
}

internal class StateSet private constructor(
        private val delegate: MutableSet<State>
) : MutableSet<State> by delegate {

    constructor(vararg elements: State) : this(mutableSetOf<State>(*elements))

    private var hasFinalState = false

    override fun add(element: State): Boolean {
        if (element.isFinal()) {
            hasFinalState = true
        }
        return delegate.add(element)
    }

    fun hasFinalState() = hasFinalState
}

internal class State private constructor (
        private val stateType: StateType,
        internal val transitions: MutableMap<Event,State> = mutableMapOf()
) {
    private enum class StateType {
        INTERMEDIATE,
        FINAL,
    }

    constructor(isFinal: Boolean = false)
            : this(if (isFinal) { StateType.FINAL } else { StateType.INTERMEDIATE })

    fun isFinal() = stateType == StateType.FINAL

    override fun toString() = if (stateType == StateType.INTERMEDIATE) {
            "s${hashCode()}"
        } else {
            "s${hashCode()}($stateType)"
        }
}

internal data class Event private constructor (
        private val eventType: EventType,
        private val ion: IonValue? = null
) {
    private enum class EventType {
        ANY,
        NOOP,
        IONVALUE,
    }

    constructor(ion: IonValue) : this(EventType.IONVALUE, ion)

    companion object {
        val ANY = Event(EventType.ANY)
        val NOOP = Event(EventType.NOOP)
    }

    override fun toString() = if (eventType == EventType.IONVALUE) { "$ion" } else { "$eventType" }
}

