/*
 * Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import com.amazon.ion.IonValue
import com.amazon.ionschema.internal.util.IntRange

private val OPEN_CONTENT_FLAG = Any()
private val DEBUG = false

/**
 * Builder that provides a simple API for constructing a [StateMachine].
 */
internal class StateMachineBuilder {
    private val initialState = State(IntRange.OPTIONAL)
    private val states = mutableSetOf(initialState)
    private var openContent: Any? = null

    fun withOpenContent(): StateMachineBuilder {
        openContent = OPEN_CONTENT_FLAG
        return this
    }

    fun addTransition(fromState: State?, event: Event, toState: State): StateMachineBuilder {
        val initialOrFromState = fromState ?: initialState
        if (!states.contains(initialOrFromState)) {
            states.add(initialOrFromState)
        }
        initialOrFromState.addTransition(event, toState)

        if (!states.contains(toState)) {
            states.add(toState)
        }

        // for states that may recur, auto-add a transition back to itself
        if (toState.isRecurring()) {
            toState.addTransition(event, toState)
        }
        return this
    }

    fun build(): StateMachine {
        return StateMachine(states, initialState, openContent)
    }
}

/**
 * Implements a Non-deterministic Finite Automata (NFA) based on an algorithm developed
 * by Ken Thompson.  With thanks to Russ Cox for a very informative writeup.
 *
 * This implementation determines whether a given Iterator<IonValue> is valid against
 * the states and transitions of the state machine.  It implements NFAs not by converting
 * them into DFAs in advance, but rather by allowing the machine to progress in multiple states
 * (see []StateSet]) concurrently.  This has the added benefit of avoiding the need to backtrack.
 *
 * A state machine instance always has at least one state, the initial state.  As states are
 * visited, a counter (visitCount) is incremented in order to enforce occurrence expectations.
 * Once reachable, an optional state is preemptively visited with visitCount equal to 0.
 * If a state machine is configured to support open content, an incoming open content event
 * results in no change to the state set.
 *
 * To illustrate, consider a state machine with states A, B, C, and D
 * (where A and B are required, C is optional, and D is expected 0..3 times),
 * and corresponding events a, b, c, d transition into states
 * of the corresponding letter.  The state machine proceeds as follows:
 *
 * Event | State set (each state represented by state:visitCount)
 * ------+-------------------------------------------------------
 *       | (initial state)
 *    a  | A:1
 *    b  | B:1, C:0, D:0   (preemptive visit to optional states C and D) --> matches!
 *
 * or:
 *
 * Event | State set
 * ------+-------------------------------------------------------
 *       | (initial state)
 *    a  | A:1
 *    b  | B:1, C:0, D:0
 *    c  | C:1, D:0 --> matches!
 *
 * or:
 *
 * Event | State set
 * ------+-------------------------------------------------------
 *       | (initial state)
 *    a  | A:1
 *    b  | B:1, C:0, D:0
 *    d  | D:1 --> matches!
 *
 * or:
 *
 * Event | State set
 * ------+-------------------------------------------------------
 *       | (initial state)
 *    a  | A:1
 *    b  | B:1, C:0, D:0
 *    c  | C:1, D:0
 *    d  | D:1
 *    d  | D:2
 *    d  | D:3 --> matches!
 *
 * or:
 *
 * Event | State set
 * ------+-------------------------------------------------------
 *       | (initial state)
 *    a  | A:1
 *    b  | B:1, C:0, D:0
 *    d  | D:1
 *    d  | D:2
 *    d  | D:3
 *    d  | D:4 --> no match, too many 'd' events
 *
 * @see https://swtch.com/~rsc/regexp/regexp1.html
 * @see "Regular Expression Search Algorithm" by Ken Thompson
 *      Communications of the ACM 11(6) (June 1968), pp. 419–422
 *      http://doi.acm.org/10.1145/363347.363387
 */
internal class StateMachine(
        private val states: Set<State>,
        private val initialState: State,
        private val openContent: Any?
) {
    fun matches(iter: Iterator<IonValue>): Boolean {
        if (DEBUG) { println(this) }

        val stateSet = StateSet(initialState, openContent)
        initialState.transitionToOptionalStates(stateSet)
        stateSet.applyVisits()

        if (DEBUG) { debug(stateSet) }
        iter.forEach { ion ->
            stateSet.forEach { state, _ ->
                state.transition(EventIonValue(ion), stateSet)
            }
            stateSet.applyVisits()
            if (DEBUG) { debug(stateSet, ion) }
        }

        return stateSet.hasFinalState
    }

    private fun debug(stateSet: StateSet, value: IonValue? = null) {
        print(if (stateSet.hasFinalState) { "F" } else { "." })
        if (value == null) {
            print(" <init> --> ")
        } else {
            print(" $value --> ")
        }
        stateSet.asSequence()
            .sortedBy { it.key.stateId }
            .forEach {
                print("${it.key.stateId}:${stateSet.visitCount(it.key)}")
                print(" ")
            }
        println()
    }

    override fun toString(): String {
        val sb = StringBuilder()
        states.asSequence()
            .sortedBy { it.stateId }
            .forEach { sb.append(it) }
        return sb.toString()
    }

    // encapsulates state tracking logic, plus intelligent tracking of "are we in a valid end state"
    internal inner class StateSet constructor(
            initialState: State,
            private val openContent: Any? = null
    ) {
        private val newStates = mutableMapOf(initialState to 0)

        internal var hasFinalState = calcHasFinalState()
        private var prevStates = mutableMapOf<State, Int>()
        private val newStateVisits = mutableMapOf<State, Int>()

        fun visit(state: State, preemptiveVisit: Boolean = false) {
            newStateVisits[state] = when {
                !preemptiveVisit -> 1
                else -> newStateVisits[state] ?: 0
            }
        }

        fun visitCount(state: State) = prevStates[state] ?: 0

        fun applyVisits() {
            newStateVisits.forEach { state, visits ->
                newStates[state] = (prevStates[state] ?: 0) + visits
            }
            newStateVisits.clear()
            hasFinalState = calcHasFinalState()

            prevStates = newStates.toMutableMap()
            openContent ?: newStates.clear()
        }

        private fun calcHasFinalState() = when {
            // special case where there is a single state (the initialState),
            // and newStates has exactly one state (which must also be the initialState)
            this@StateMachine.states.size == 1 && newStates.size == 1 -> true
            else -> {
                newStates.asSequence()
                        .takeWhile { stateVisit ->
                            !stateVisit.key.isFinal(stateVisit.value)
                        }
                        .count() != newStates.size
            }
        }

        fun asSequence(): Sequence<Map.Entry<State, Int>> = prevStates.asSequence()
        fun forEach(action: (State, Int) -> Unit) = prevStates.forEach(action)
    }
}

private var stateCnt = 0

// represents a state in the state machine model
internal class State private constructor(
        private val occurs: IntRange,
        private val type: StateType,
        private val transitions: MutableMap<Event,State> = mutableMapOf()
) {
    internal val stateId = "S$stateCnt"
    init {
        stateCnt += 1
    }

    constructor(occurs: IntRange, isFinal: Boolean = false)
            : this(occurs, if (isFinal) { StateType.FINAL } else { StateType.INTERMEDIATE })

    internal fun addTransition(event: Event, toState: State) {
        transitions[event] = toState
    }

    internal fun transition(event: Event? = null, stateSet: StateMachine.StateSet) {
        val value = (event as? EventIonValue)?.ion
        val visitCount = stateSet.visitCount(this)
        transitions.forEach { eventCandidate, toState ->
            when {
                   (toState != this && occurs.lower <= visitCount)       // allow proceeding to the next state if we've reached minOccurs
                || (toState == this && occurs.upper >  visitCount) -> {  // allow repeat of current state if we haven't yet reached maxOccurs

                    if (eventCandidate.matches(value)) {
                        stateSet.visit(toState)
                        if (toState.occurs.lower <= stateSet.visitCount(toState) + 1) {
                            toState.transitionToOptionalStates(stateSet)
                        }
                    }
                }
            }
        }
    }

    internal fun transitionToOptionalStates(stateSet: StateMachine.StateSet): List<State> =
            transitions.values
                    .filterNot { it == this }
                    .filter { it.occurs.contains(0) }
                    .onEach {
                        stateSet.visit(it, preemptiveVisit = true)
                        it.transitionToOptionalStates(stateSet)
                    }

    internal fun isFinal(visits: Int) =
            type == StateType.FINAL
                    && occurs.contains(visits)

    internal fun isRecurring() = occurs.upper > 1

    override fun toString(): String {
        val sb = StringBuilder("$stateId:$occurs")
        if (type == StateType.FINAL) {
            sb.append(" <final>")
        }
        sb.appendln()
        transitions.forEach {
            sb.appendln("  ${it.key} --> ${it.value.stateId}")
        }
        return sb.toString()
    }

    private enum class StateType {
        INTERMEDIATE,
        FINAL,
    }
}

// base class for all event types
internal abstract class Event {
    abstract fun matches(value: IonValue?): Boolean
}

// represents an event that corresponds to a specific Ion value
internal class EventIonValue(internal val ion: IonValue) : Event() {
    override fun matches(value: IonValue?) = ion == value
    override fun toString() = "$ion"
}

// represents an event that corresponds to an Ion Schema Type
internal data class EventSchemaType(private val typeResolver: () -> com.amazon.ionschema.Type) : Event() {
    override fun matches(value: IonValue?) = if (value == null) { false } else { typeResolver().isValid(value) }
    override fun toString() = "type: ${typeResolver().name}"
}

