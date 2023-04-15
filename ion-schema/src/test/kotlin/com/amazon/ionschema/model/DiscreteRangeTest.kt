package com.amazon.ionschema.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DiscreteRangeTest {

    @Test fun `degenerate interval should be valid`() { DiscreteIntRange(0, 0) }
    @Test fun `proper, bounded interval should be valid`() { DiscreteIntRange(0, 9) }
    @Test fun `upper-bounded interval should be valid`() { DiscreteIntRange(0, null) }
    @Test fun `lower-bounded interval should be valid`() { DiscreteIntRange(null, 0) }
    @Test fun `both limits unbounded should throw IllegalArgumentException`() { assertThrows<IllegalArgumentException> { DiscreteIntRange(null, null) } }
    @Test fun `empty interval should throw IllegalArgumentException`() { assertThrows<IllegalArgumentException> { DiscreteIntRange(1, 0) } }

    fun testContainsArgs() = listOf(
        case(DiscreteIntRange(2, 4), value = 1, isContained = false),
        case(DiscreteIntRange(2, 4), value = 2, isContained = true),
        case(DiscreteIntRange(2, 4), value = 3, isContained = true),
        case(DiscreteIntRange(2, 4), value = 4, isContained = true),
        case(DiscreteIntRange(2, 4), value = 5, isContained = false),
        case(DiscreteIntRange(null, 4), value = -99, isContained = true),
        case(DiscreteIntRange(null, 4), value = -1, isContained = true),
        case(DiscreteIntRange(null, 4), value = 0, isContained = true),
        case(DiscreteIntRange(null, 4), value = 4, isContained = true),
        case(DiscreteIntRange(null, 4), value = 5, isContained = false),
        case(DiscreteIntRange(-3, null), value = -99, isContained = false),
        case(DiscreteIntRange(-3, null), value = -3, isContained = true),
        case(DiscreteIntRange(-3, null), value = 0, isContained = true),
        case(DiscreteIntRange(-3, null), value = 4, isContained = true),
        case(DiscreteIntRange(-3, null), value = 99, isContained = true),
        case(DiscreteIntRange(8, 8), value = 7, isContained = false),
        case(DiscreteIntRange(8, 8), value = 8, isContained = true),
        case(DiscreteIntRange(8, 8), value = 9, isContained = false),
    )
    @MethodSource("testContainsArgs")
    @ParameterizedTest(name = "contains check of {1} in {0} should be {2}")
    fun testContains(range: DiscreteIntRange, value: Int, isContained: Boolean) {
        assertEquals(value in range, isContained)
    }

    fun testIntersectArgs() = listOf(
        // identical ranges
        case(a = DiscreteIntRange(0, 0), b = DiscreteIntRange(0, 0), expected = DiscreteIntRange(0, 0)),
        case(a = DiscreteIntRange(1, 2), b = DiscreteIntRange(1, 2), expected = DiscreteIntRange(1, 2)),
        case(a = DiscreteIntRange(null, 3), b = DiscreteIntRange(null, 3), expected = DiscreteIntRange(null, 3)),
        case(a = DiscreteIntRange(4, null), b = DiscreteIntRange(4, null), expected = DiscreteIntRange(4, null)),
        // one range completely subsumes the other
        case(a = DiscreteIntRange(0, 1), b = DiscreteIntRange(0, 2), expected = DiscreteIntRange(0, 1)),
        case(a = DiscreteIntRange(1, 2), b = DiscreteIntRange(null, 2), expected = DiscreteIntRange(1, 2)),
        case(a = DiscreteIntRange(2, 3), b = DiscreteIntRange(2, null), expected = DiscreteIntRange(2, 3)),
        // overlap, but neither subsumes the other
        case(a = DiscreteIntRange(0, null), b = DiscreteIntRange(null, 0), expected = DiscreteIntRange(0, 0)),
        case(a = DiscreteIntRange(0, 4), b = DiscreteIntRange(1, 5), expected = DiscreteIntRange(1, 4)),
        case(a = DiscreteIntRange(1, 5), b = DiscreteIntRange(2, null), expected = DiscreteIntRange(2, 5)),
        case(a = DiscreteIntRange(2, 6), b = DiscreteIntRange(null, 4), expected = DiscreteIntRange(2, 4)),
        case(a = DiscreteIntRange(3, null), b = DiscreteIntRange(null, 5), expected = DiscreteIntRange(3, 5)),
        // no overlap
        case(a = DiscreteIntRange(0, 0), b = DiscreteIntRange(1, 1), expected = null),
        case(a = DiscreteIntRange(0, 0), b = DiscreteIntRange(1, null), expected = null),
        case(a = DiscreteIntRange(0, 0), b = DiscreteIntRange(null, -1), expected = null),
    )
    @MethodSource("testIntersectArgs")
    @ParameterizedTest(name = "intersect of {0} and {1} should be {2}")
    fun testIntersect(range1: DiscreteIntRange, range2: DiscreteIntRange, expectedIntersect: DiscreteIntRange?) {
        assertEquals(expectedIntersect, range1.intersect(range2))
        assertEquals(expectedIntersect, range2.intersect(range1))
    }

    fun testNegateArgs() = listOf(
        case(original = DiscreteIntRange(0, 0), expected = DiscreteIntRange(0, 0)), // degenerate 0 interval
        case(original = DiscreteIntRange(1, 1), expected = DiscreteIntRange(-1, -1)), // trivial non-zero interval
        case(original = DiscreteIntRange(-2, 2), expected = DiscreteIntRange(-2, 2)), // symmetric (negates to self), proper bounded interval
        case(original = DiscreteIntRange(-3, 4), expected = DiscreteIntRange(-4, 3)), // non-symmetric, proper, bounded interval
        case(original = DiscreteIntRange(-5, null), expected = DiscreteIntRange(null, 5)), // lower-bounded
        case(original = DiscreteIntRange(null, -6), expected = DiscreteIntRange(6, null)), // upper-bounded
    )
    @MethodSource("testNegateArgs")
    @ParameterizedTest(name = "negate {0} should be {1}")
    fun testNegate(original: DiscreteIntRange, expectedNegation: DiscreteIntRange) {
        assertEquals(expectedNegation, original.negate())
    }

    companion object {
        /** case for `testContains` */
        private fun case(range: DiscreteIntRange, value: Int, isContained: Boolean) = arguments(range, value, isContained)
        /** case for `testNegate` */
        private fun case(original: DiscreteIntRange, expected: DiscreteIntRange) = arguments(original, expected)
        /** case for `testIntersect` */
        private fun case(a: DiscreteIntRange, b: DiscreteIntRange, expected: DiscreteIntRange?) = arguments(a, b, expected)
    }
}
