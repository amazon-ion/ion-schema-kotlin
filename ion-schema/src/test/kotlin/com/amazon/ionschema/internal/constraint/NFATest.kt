package com.amazon.ionschema.internal.constraint

import com.amazon.ionschema.internal.constraint.NFA.State.Final
import com.amazon.ionschema.internal.constraint.NFA.State.Initial
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Collections

class NFATest {

    /** Helper function to construct a State */
    private fun state(id: Int, min: Int = 1, max: Int = min) = NFA.State.Intermediate(id, id::equals, reentryCondition = { it <= max }, exitCondition = { it >= min })

    @Test
    fun `trivial case should be handled correctly`() {
        val nfa = NFA(mapOf(Initial to setOf(Final)))

        assertTrue(nfa.matches(listOf()))

        assertFalse(nfa.matches(listOf(1)))
        assertFalse(nfa.matches(listOf(1, 2)))
        assertFalse(nfa.matches(listOf(1, 2, 3)))
    }

    @Test
    fun `simple cases should be handled correctly`() {
        val nfa = NFA(
            mapOf(
                Initial to setOf(state(1)),
                state(1) to setOf(state(2)),
                state(2) to setOf(state(3)),
                state(3) to setOf(Final),
            )
        )

        assertTrue(nfa.matches(listOf(1, 2, 3)))

        assertFalse(nfa.matches(listOf()))
        assertFalse(nfa.matches(listOf(1)))
        assertFalse(nfa.matches(listOf(1, 2)))
        assertFalse(nfa.matches(listOf(1, 2, 3, 3)))
        assertFalse(nfa.matches(listOf(1, 2, 3, 4)))
    }

    @Test
    fun `branching graph cases should be handled correctly`() {
        val nfa = NFA(
            mapOf(
                Initial to setOf(state(1)),
                state(1) to setOf(state(2), state(3)),
                state(2) to setOf(Final),
                state(3) to setOf(Final),
            )
        )

        assertTrue(nfa.matches(listOf(1, 2)))
        assertTrue(nfa.matches(listOf(1, 3)))

        assertFalse(nfa.matches(listOf()))
        assertFalse(nfa.matches(listOf(1)))
        assertFalse(nfa.matches(listOf(1, 2, 3)))
        assertFalse(nfa.matches(listOf(1, 3, 2)))
    }

    @Test
    fun `cycles should be handled correctly`() {
        val nfa = NFA(
            mapOf(
                Initial to setOf(state(1)),
                state(1) to setOf(state(2)),
                state(2) to setOf(state(3)),
                state(3) to setOf(state(1), Final),
            )
        )
        assertTrue(nfa.matches(listOf(1, 2, 3)))
        assertTrue(nfa.matches(listOf(1, 2, 3, 1, 2, 3)))
        assertTrue(nfa.matches(listOf(1, 2, 3, 1, 2, 3, 1, 2, 3)))
    }

    @Test
    fun `loops should be handled correctly`() {
        val s1 = state(1, max = Int.MAX_VALUE)

        val nfa = NFA(
            mapOf(
                Initial to setOf(s1),
                s1 to setOf(s1, Final),
            )
        )

        assertTrue(nfa.matches(listOf(1)))
        assertTrue(nfa.matches(listOf(1, 1)))
        assertTrue(nfa.matches(listOf(1, 1, 1)))
        assertTrue(nfa.matches(Collections.nCopies(12345, 1)))
    }

    @Test
    fun `max number of loops should be enforceable with the re-entry condition`() {
        val s1 = state(1, max = 3)
        val nfa = NFA(
            mapOf(
                Initial to setOf(s1),
                s1 to setOf(s1, Final),
            )
        )

        assertTrue(nfa.matches(listOf(1)))
        assertTrue(nfa.matches(listOf(1, 1)))
        assertTrue(nfa.matches(listOf(1, 1, 1)))
        assertFalse(nfa.matches(listOf(1, 1, 1, 1)))
    }

    @Test
    fun `looping should be required when there is an appropriate exit condition`() {
        val s1 = state(1, min = 3, max = Int.MAX_VALUE)
        val nfa = NFA(
            mapOf(
                Initial to setOf(s1),
                s1 to setOf(s1, Final),
            )
        )

        assertFalse(nfa.matches(listOf(1)))
        assertFalse(nfa.matches(listOf(1, 1)))
        assertTrue(nfa.matches(listOf(1, 1, 1)))
        assertTrue(nfa.matches(listOf(1, 1, 1, 1)))
    }

    @Test
    fun `state transition graph must have Initial state`() {
        assertThrows<IllegalArgumentException> {
            NFA(
                mapOf(
                    state(1) to setOf(state(2)),
                    state(2) to setOf(state(1), Final),
                )
            )
        }
    }

    @Test
    fun `state transition graph must have at least one transition from Initial state`() {
        assertThrows<IllegalArgumentException> {
            NFA(
                mapOf(
                    Initial to emptySet<NFA.State<Int>>(),
                    state(1) to setOf(state(2)),
                    state(2) to setOf(state(1), Final),
                )
            )
        }
    }

    @Test
    fun `state transition graph must not have any transition to an unknown state`() {
        assertThrows<IllegalArgumentException> {
            NFA(
                mapOf(
                    Initial to setOf(state(1)),
                    state(1) to setOf(state(2)),
                    state(2) to setOf(state(3), Final),
                )
            )
        }
    }

    @Test
    fun `state transition graph must have a transition to Final state`() {
        assertThrows<IllegalArgumentException> {
            NFA(
                mapOf(
                    Initial to setOf(state(1)),
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
                    Initial to setOf(state(1)),
                    state(1) to setOf(Final),
                    Final to setOf(state(1))
                )
            )
        }
    }
}
