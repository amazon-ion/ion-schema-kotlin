/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazon.ionschema.internal.constraint

/**
 * A Non-deterministic Finite-State Automaton for evaluating regular languages (and analogues thereof).
 *
 * This implementation uses the following algorithm.
 * > the NFA consumes a string of input symbols, one by one. In each step, whenever two or more transitions are applicable,
 * > it "clones" itself into appropriately many copies, each one following a different transition. If no transition is
 * > applicable, the current copy is in a dead end, and it "dies". If, after consuming the complete input, any of the
 * > copies is in an accept state, the input is accepted, else, it is rejected.
 *
 * See [Nondeterministic finite automaton](https://en.wikipedia.org/wiki/Nondeterministic_finite_automaton).
 *
 * This implementation is designed to track which state(s) are visited as well as how many consecutive visits have been
 * made to any given state. This specialization is intended to make it easier to support cases where an event could occur
 * a variable number of times (eg. regex `a{2,4}`).
 *
 * This implementation does not check for matches that are less than the full stream of events.
 *
 * For an example of how to model things using this class, see [OrderedElementsNfaStatesBuilder].
 */
internal class NFA<Event : Any>(
    /**
     * An adjacency list describing the transitions between states in the NFA. The adjacency list is modeled as a map of
     * source to list of destinations. Any intermediate state that appears as a destination must also appear as a key in
     * the map. The map must contain [NFA.State.Initial] as a key. There may be no transitions out of [NFA.State.Final].
     */
    private val transitions: Map<State<Event>, Set<State<Event>>>,
) {

    init {
        require(!transitions[State.Initial].isNullOrEmpty()) { "Invalid graph: no transition from initial state." }
        require(transitions[State.Final].isNullOrEmpty()) { "Invalid graph: no transitions allowed from final state." }
        transitions.values.flatten().let { destinations ->
            val unknownDestinations = destinations.filter { it !in transitions.keys && it != State.Final }.distinct()
            require(unknownDestinations.isEmpty()) { "Invalid graph: transitions to unknown states: $unknownDestinations" }
            require(State.Final in destinations) { "Invalid graph: no transition to final state." }
        }
    }

    companion object { private val END_OF_STREAM_EVENT: Nothing? = null }
    private val INITIAL_STATE = setOf(StateVisitCount<Event>(State.Initial, 1))

    sealed class State<in Event : Any>(val debugId: String) {
        open fun canEnter(event: Event?): Boolean = false
        open fun canReenter(visits: Int): Boolean = false
        open fun canExit(visits: Int): Boolean = false
        final override fun toString(): String = debugId

        object Initial : State<Any>("I") {
            override fun canExit(visits: Int): Boolean = true
        }

        object Final : State<Any>("F") {
            override fun canEnter(event: Any?): Boolean = event == END_OF_STREAM_EVENT
        }

        class Intermediate<Event : Any>(
            /** Unique identifier for this state. */
            val id: Int,
            /** Condition on the [Event] value for entering this state. */
            val entryCondition: (Event) -> Boolean,
            /** Condition on number of visits for re-entering this state. Only applies on a loop (i.e. not S1->S2->S1). */
            val reentryCondition: (Int) -> Boolean,
            /** Condition on number of visits for leaving this state. Does not apply when following a loop (ie. S1->S1). */
            val exitCondition: (Int) -> Boolean,
        ) : State<Event>("S$id") {

            override fun canEnter(event: Event?): Boolean = event != null && entryCondition(event)
            override fun canReenter(visits: Int): Boolean = reentryCondition(visits)
            override fun canExit(visits: Int): Boolean = exitCondition(visits)

            override fun hashCode(): Int = id.hashCode()
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                return other is Intermediate<*> && id == other.id
            }
        }
    }

    /** Models the number of times a given state has been visited for a possible path of execution */
    private data class StateVisitCount<in Event : Any>(val state: State<Event>, val visits: Int) {
        override fun toString(): String = "${state.debugId}:$visits"
    }

    private fun Set<StateVisitCount<Event>>.transition(event: Event?, eventDebugString: String? = null): Set<StateVisitCount<Event>> {
        val newStates = mutableSetOf<StateVisitCount<Event>>()

        // Cache the results of testing the entry conditions for states so that we aren't duplicating any work.
        val entryConditionCache = mutableMapOf<State<Event>, Boolean>()

        this@transition.forEach { (transitionFromState, visits) ->
            // Cache the result of canExit, just in case someone has an expensive test here.
            val canExit = transitionFromState.canExit(visits)

            transitions[transitionFromState]?.forEach { transitionToState ->
                if (transitionFromState == transitionToState) {
                    if (transitionToState.canReenter(visits + 1)) {
                        // Can we enter this same state one more time?
                        val canEnter = entryConditionCache.getOrPut(transitionToState) { transitionToState.canEnter(event) }
                        if (canEnter) newStates.add(StateVisitCount(transitionToState, visits + 1))
                    }
                } else if (canExit) {
                    // Can we exit the current state yet, and if so, can we enter the "to" state?
                    val canEnter = entryConditionCache.getOrPut(transitionToState) { transitionToState.canEnter(event) }
                    if (canEnter) newStates.add(StateVisitCount(transitionToState, 1))
                }
            }
        }
        eventDebugString?.let { println("${it.padStart(4)} : $newStates") }
        return newStates.toSet()
    }

    /**
     * Tests if a stream of [Event] is recognized by this state machine.
     */
    fun matches(events: Iterable<Event>, debug: Boolean = false): Boolean {
        if (debug) {
            println()
            println(transitions.entries.joinToString("\n") { (k, v) -> "${"$k".padEnd(3)} -> $v" })
            println("Transitions for input: $events")
            println("     : $INITIAL_STATE")
        }
        return events.foldIndexed(INITIAL_STATE) { idx, states, event -> states.transition(event, if (debug) "$idx" else null) }
            .transition(END_OF_STREAM_EVENT, if (debug) "END" else null)
            .any { (state, _) -> state == State.Final }
    }
}
