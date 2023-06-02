package com.amazon.ionschema.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(Lifecycle.PER_CLASS)
class ContinuousRangeTest {

    @TestFactory
    fun testConstructingValidRanges() = listOf(
        closed(0.0) to closed(0.0),
        closed(0.0) to closed(1.0),
        open(0.0) to closed(1.0),
        closed(0.0) to open(1.0),
        open(0.0) to open(1.0),
        closed(0.0) to unbounded(),
        open(0.0) to unbounded(),
        unbounded() to closed(1.0),
        unbounded() to open(1.0),
    ).map { (min, max) -> "[constructor] ContinuousRange($min, $max) should be valid" { ContinuousRange(min, max) } }

    fun invalidRanges() = listOf(
        open(0.0) to closed(0.0),
        closed(0.0) to open(0.0),
        open(0.0) to open(0.0),
        unbounded() to unbounded(),
        closed(0.0) to closed(-1.0),
        open(0.0) to closed(-1.0),
        closed(0.0) to open(-1.0),
        open(0.0) to open(-1.0),
    ).map { arguments(it.first, it.second) }
    @ParameterizedTest(name = "[constructor] ContinuousRange({0}, {1}) should not be valid")
    @MethodSource("invalidRanges")
    fun `ContinuousRange should be invalid`(min: ContinuousRange.Limit<Double>, max: ContinuousRange.Limit<Double>) {
        assertThrows<IllegalArgumentException> { ContinuousRange(min, max) }
    }

    @TestFactory
    fun testContains() = listOf(
        case(ContinuousRange(closed(2.0), closed(4.0)), value = 1.0, isContained = false),
        case(ContinuousRange(closed(2.0), closed(4.0)), value = 2.0, isContained = true),
        case(ContinuousRange(closed(2.0), closed(4.0)), value = 3.0, isContained = true),
        case(ContinuousRange(closed(2.0), closed(4.0)), value = 4.0, isContained = true),
        case(ContinuousRange(closed(2.0), closed(4.0)), value = 5.0, isContained = false),
        case(ContinuousRange(unbounded(), closed(4.0)), value = -99.0, isContained = true),
        case(ContinuousRange(unbounded(), closed(4.0)), value = -1.0, isContained = true),
        case(ContinuousRange(unbounded(), closed(4.0)), value = 0.0, isContained = true),
        case(ContinuousRange(unbounded(), closed(4.0)), value = 4.0, isContained = true),
        case(ContinuousRange(unbounded(), closed(4.0)), value = 5.0, isContained = false),
        case(ContinuousRange(closed(-3.0), unbounded()), value = -99.0, isContained = false),
        case(ContinuousRange(closed(-3.0), unbounded()), value = -3.0, isContained = true),
        case(ContinuousRange(closed(-3.0), unbounded()), value = 0.0, isContained = true),
        case(ContinuousRange(closed(-3.0), unbounded()), value = 4.0, isContained = true),
        case(ContinuousRange(closed(-3.0), unbounded()), value = 99.0, isContained = true),
        case(ContinuousRange(closed(8.0), closed(8.0)), value = 7.0, isContained = false),
        case(ContinuousRange(closed(8.0), closed(8.0)), value = 8.0, isContained = true),
        case(ContinuousRange(closed(8.0), closed(8.0)), value = 9.0, isContained = false),
        case(ContinuousRange(open(2.0), open(4.0)), value = 1.0, isContained = false),
        case(ContinuousRange(open(2.0), open(4.0)), value = 2.0, isContained = false),
        case(ContinuousRange(open(2.0), open(4.0)), value = 2.1, isContained = true),
        case(ContinuousRange(open(2.0), open(4.0)), value = 3.0, isContained = true),
        case(ContinuousRange(open(2.0), open(4.0)), value = 3.9, isContained = true),
        case(ContinuousRange(open(2.0), open(4.0)), value = 4.0, isContained = false),
        case(ContinuousRange(open(2.0), open(4.0)), value = 5.0, isContained = false),
    ).map { (range, value, isContained) -> "[contains] $value in $range should be $isContained" { assertEquals(isContained, value in range) } }

    // @ParameterizedTest(name = "[intersect] intersection of {0} and {1} should be {2}")
    // @MethodSource("intersectCases")
    fun testIntersect(a: ContinuousRange<Double>, b: ContinuousRange<Double>, expect: ContinuousRange<Double>?) {
        assertEquals(expect, a.intersect(b))
        assertEquals(expect, b.intersect(a))
    }

    @TestFactory
    fun testIntersect() = listOf(
        // //// Comparing ranges with same limit types and different numbers //////
        // Degenerate case
        listOf((closed(0.0) to closed(0.0)).let { intersectCase(it, it, it) }),
        // Identical cases
        generateRanges(0.0, 1.0).map { intersectCase(it, it, it) },
        // Subsume cases
        generateRanges(1.0, 3.0).zip(generateRanges(0.0, 4.0)) { a, b -> intersectCase(a, b, a) },
        generateRanges(1.0, 2.0).zip(generateRanges(1.0, 8.0)) {
            a, b ->
            intersectCase(a, b, b.first to a.second)
        },
        // Non-subsume overlap cases
        generateRanges(5.0, 7.0).zip(generateRanges(6.0, 8.0)) {
            a, b ->
            intersectCase(a, b, b.first to a.second)
        },
        // //// Comparing ranges with same number and different limit types //////
        generateRanges(1.0, 2.0).let {
            it.cartesianProduct(it).map {
                (a, b) ->
                intersectCase(a, b, narrowest(a.first, b.first) to narrowest(a.second, b.second))
            }
        },
    ).flatten().map { (a, b, expected) ->
        "[intersect] intersection of $a and $b should be $expected" {
            assertEquals(expected, a.intersect(b))
            assertEquals(expected, b.intersect(a))
        }
    }

    companion object {
        private operator fun String.invoke(block: () -> Unit) = DynamicTest.dynamicTest(this, block)

        fun closed(d: Double) = d.let { ContinuousRange.Limit.Closed(d) }
        fun open(d: Double) = d.let { ContinuousRange.Limit.Open(d) }
        fun unbounded() = ContinuousRange.Limit.Unbounded

        /** case for `testContains` */
        private fun case(range: ContinuousRange<Double>, value: Double, isContained: Boolean) = Triple(range, value, isContained)

        /** creates test case args for intersect tests */
        private fun intersectCase(
            a: Pair<ContinuousRange.Limit<Double>, ContinuousRange.Limit<Double>>,
            b: Pair<ContinuousRange.Limit<Double>, ContinuousRange.Limit<Double>>,
            expect: Pair<ContinuousRange.Limit<Double>, ContinuousRange.Limit<Double>>?
        ) = Triple(ContinuousRange(a.first, a.second), ContinuousRange(b.first, b.second), expect?.let { ContinuousRange(it.first, it.second) })

        /** Generates ranges with every possible combination of limit types */
        private fun generateRanges(a: Double, b: Double): List<Pair<ContinuousRange.Limit<Double>, ContinuousRange.Limit<Double>>> = listOf(
            closed(a) to closed(b), // closed
            open(a) to open(b), // open
            closed(a) to open(b), // open_UPPER
            open(a) to closed(b), // open_LOWER
            unbounded() to open(b), // HALF_BOUND_open_UPPER
            unbounded() to closed(b), // HALF_BOUND_closed_UPPER
            open(a) to unbounded(), // HALF_BOUND_open_LOWER
            closed(a) to unbounded(), // HALF_BOUND_closed_LOWER
        )

        /** Cartesian product of two collections */
        private fun <T, U> Collection<T>.cartesianProduct(other: Collection<U>) = flatMap { a -> other.map { b -> a to b } }

        /** determines the narrowest limit based on type, and ignoring the actual number */
        private fun <T : Comparable<T>> narrowest(a: ContinuousRange.Limit<T>, b: ContinuousRange.Limit<T>): ContinuousRange.Limit<T> = when {
            a is ContinuousRange.Limit.Open -> a
            b is ContinuousRange.Limit.Open -> b
            a is ContinuousRange.Limit.Closed -> a
            b is ContinuousRange.Limit.Closed -> b
            else -> a
        }
    }
}
