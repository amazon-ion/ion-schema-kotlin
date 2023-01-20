package com.amazon.ionschema.internal.constraint

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Collections

class NFATest {

    /** Helper function to construct a State */
    private fun state(id: Int, min: Int = 1, max: Int = min): NFA.State<Int, String> = NFA.State.Intermediate(id, { NFA.State.Decision(it == id) }, reentryCondition = { it <= max }, exitCondition = { it >= min })

    @Test
    fun `trivial case should be handled correctly`() {
        val nfa = NFA(mapOf(NFA.State.initial<Int>() to setOf(NFA.State.final())))

        assertTrue(nfa.matches(listOf()) is NFA.Outcome.IsMatch)

        assertTrue(nfa.matches(listOf(1)) is NFA.Outcome.IsNotMatch)
        assertTrue(nfa.matches(listOf(1, 2)) is NFA.Outcome.IsNotMatch)
        assertTrue(nfa.matches(listOf(1, 2, 3)) is NFA.Outcome.IsNotMatch)
    }

    @Test
    fun `simple cases should be handled correctly`() {
        val nfa = NFA(
            mapOf(
                NFA.State.initial<Int>() to setOf(state(1)),
                state(1) to setOf(state(2)),
                state(2) to setOf(state(3)),
                state(3) to setOf(NFA.State.final()),
            )
        )

        assertTrue(nfa.matches(listOf(1, 2, 3)) is NFA.Outcome.IsMatch)

        assertTrue(nfa.matches(listOf()) is NFA.Outcome.IsNotMatch)
        assertTrue(nfa.matches(listOf(1)) is NFA.Outcome.IsNotMatch)
        assertTrue(nfa.matches(listOf(1, 2)) is NFA.Outcome.IsNotMatch)
        assertTrue(nfa.matches(listOf(1, 2, 3, 3)) is NFA.Outcome.IsNotMatch)
        assertTrue(nfa.matches(listOf(1, 2, 3, 4)) is NFA.Outcome.IsNotMatch)
    }

    @Test
    fun `branching graph cases should be handled correctly`() {
        val nfa = NFA(
            mapOf(
                NFA.State.initial<Int>() to setOf(state(1)),
                state(1) to setOf(state(2), state(3)),
                state(2) to setOf(NFA.State.final()),
                state(3) to setOf(NFA.State.final()),
            )
        )

        assertTrue(nfa.matches(listOf(1, 2)) is NFA.Outcome.IsMatch)
        assertTrue(nfa.matches(listOf(1, 3)) is NFA.Outcome.IsMatch)

        assertTrue(nfa.matches(listOf()) is NFA.Outcome.IsNotMatch)
        assertTrue(nfa.matches(listOf(1)) is NFA.Outcome.IsNotMatch)
        assertTrue(nfa.matches(listOf(1, 2, 3)) is NFA.Outcome.IsNotMatch)
        assertTrue(nfa.matches(listOf(1, 3, 2)) is NFA.Outcome.IsNotMatch)
    }

    @Test
    fun `cycles should be handled correctly`() {
        val nfa = NFA(
            mapOf(
                NFA.State.initial<Int>() to setOf(state(1)),
                state(1) to setOf(state(2)),
                state(2) to setOf(state(3)),
                state(3) to setOf(state(1), NFA.State.final()),
            )
        )
        assertTrue(nfa.matches(listOf(1, 2, 3)) is NFA.Outcome.IsMatch)
        assertTrue(nfa.matches(listOf(1, 2, 3, 1, 2, 3)) is NFA.Outcome.IsMatch)
        assertTrue(nfa.matches(listOf(1, 2, 3, 1, 2, 3, 1, 2, 3)) is NFA.Outcome.IsMatch)
    }

    @Test
    fun `loops should be handled correctly`() {
        val s1 = state(1, max = Int.MAX_VALUE)

        val nfa = NFA(
            mapOf(
                NFA.State.initial<Int>() to setOf(s1),
                s1 to setOf(s1, NFA.State.final()),
            )
        )

        assertTrue(nfa.matches(listOf(1)) is NFA.Outcome.IsMatch)
        assertTrue(nfa.matches(listOf(1, 1)) is NFA.Outcome.IsMatch)
        assertTrue(nfa.matches(listOf(1, 1, 1)) is NFA.Outcome.IsMatch)
        assertTrue(nfa.matches(Collections.nCopies(12345, 1)) is NFA.Outcome.IsMatch)
    }

    @Test
    fun `max number of loops should be enforceable with the re-entry condition`() {
        val s1 = state(1, max = 3)
        val nfa = NFA(
            mapOf(
                NFA.State.initial<Int>() to setOf(s1),
                s1 to setOf(s1, NFA.State.final()),
            )
        )

        assertTrue(nfa.matches(listOf(1)) is NFA.Outcome.IsMatch)
        assertTrue(nfa.matches(listOf(1, 1)) is NFA.Outcome.IsMatch)
        assertTrue(nfa.matches(listOf(1, 1, 1)) is NFA.Outcome.IsMatch)
        assertTrue(nfa.matches(listOf(1, 1, 1, 1)) is NFA.Outcome.IsNotMatch)
    }

    @Test
    fun `looping should be required when there is an appropriate exit condition`() {
        val s1 = state(1, min = 3, max = Int.MAX_VALUE)
        val nfa = NFA(
            mapOf(
                NFA.State.initial<Int>() to setOf(s1),
                s1 to setOf(s1, NFA.State.final<Int>()),
            )
        )

        assertTrue(nfa.matches(listOf(1)) is NFA.Outcome.IsNotMatch)
        assertTrue(nfa.matches(listOf(1, 1)) is NFA.Outcome.IsNotMatch)
        assertTrue(nfa.matches(listOf(1, 1, 1)) is NFA.Outcome.IsMatch)
        assertTrue(nfa.matches(listOf(1, 1, 1, 1)) is NFA.Outcome.IsMatch)
    }

    @Test
    fun `state transition graph must have Initial state`() {
        assertThrows<IllegalArgumentException> {
            NFA(
                mapOf(
                    state(1) to setOf(state(2)),
                    state(2) to setOf(state(1), NFA.State.final()),
                )
            )
        }
    }

    @Test
    fun `state transition graph must have at least one transition from Initial state`() {
        assertThrows<IllegalArgumentException> {
            NFA(
                mapOf(
                    NFA.State.initial<Int>() to emptySet(),
                    state(1) to setOf(state(2)),
                    state(2) to setOf(state(1), NFA.State.final()),
                )
            )
        }
    }

    @Test
    fun `state transition graph must not have any transition to an unknown state`() {
        assertThrows<IllegalArgumentException> {
            NFA(
                mapOf(
                    NFA.State.initial<Int>() to setOf(state(1)),
                    state(1) to setOf(state(2)),
                    state(2) to setOf(state(3), NFA.State.final()),
                )
            )
        }
    }

    @Test
    fun `state transition graph must have a transition to Final state`() {
        assertThrows<IllegalArgumentException> {
            NFA(
                mapOf(
                    NFA.State.initial<Int>() to setOf(state(1)),
                    state(1) to setOf(state(2)),
                    state(2) to setOf(state(1)),
                )
            )
        }
    }

    @Test
    fun `state transition graph must not have a transition from Final state`() {
        assertThrows<IllegalArgumentException> {
            NFA(
                mapOf(
                    NFA.State.initial<Int>() to setOf(state(1)),
                    state(1) to setOf(NFA.State.final()),
                    NFA.State.final<Int>() to setOf(state(1))
                )
            )
        }
    }
}
