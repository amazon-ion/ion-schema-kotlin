package com.amazon.ionschema.model

import com.amazon.ionschema.model.ContinuousRange.Limit

interface IContinuousRange<T> where T : Comparable<T>, T : Any {
    val start: Limit<T>
    val end: Limit<T>
    operator fun contains(value: T): Boolean
    fun intersect(that: ContinuousRange<T>): Pair<Limit<T>, Limit<T>>?
}

/**
 * A range over a type that is an uncountably infinite set.
 *
 * A `ContinuousRange` is a _bounded interval_ when both limits are non-null, and it is a _half-bounded interval_
 * when one of the limits is `null`. At least one of [start] and [end] must be non-null.
 *
 * A `ContinuousRange` can be _open_, _half-open_, or _closed_ depending on [Limit.exclusive] of the limits.
 *
 * A `ContinuousRange` is allowed to be a _degenerate interval_ (i.e. `start == end` when both limits are closed),
 * but it may not be an empty interval (i.e. `start == end` when either limit is open, or `start > end`).
 */
open class ContinuousRange<T : Comparable<T>> internal constructor(final override val start: Limit<T>, final override val end: Limit<T>) : IContinuousRange<T> {

    private constructor(value: Limit.Closed<T>) : this(value, value)
    internal constructor(value: T) : this(Limit.Closed(value))

    sealed class Limit<out T> {
        abstract val value: T?

        interface Bounded<T> { val value: T }
        data class Closed<T : Comparable<T>>(override val value: T) : Limit<T>(), Bounded<T>
        data class Open<T : Comparable<T>>(override val value: T) : Limit<T>(), Bounded<T>
        object Unbounded : Limit<Nothing>() {
            override val value: Nothing? get() = null

            override fun equals(other: Any?) = other is Unbounded
            override fun hashCode() = 0
        }
    }

    init {
        require(start is Limit.Bounded<*> || end is Limit.Bounded<*>) { "range may not be unbounded both above and below" }
        require(!isEmpty(start, end)) { "range may not be empty" }
    }

    /**
     * Returns the intersection of `this` `DiscreteRange` with [other].
     * If the two ranges do not intersect, returns `null`.
     */
    final override fun intersect(that: ContinuousRange<T>): Pair<Limit<T>, Limit<T>>? {
        val newStart = when {
            this.start is Limit.Unbounded -> that.start
            that.start is Limit.Unbounded -> this.start
            this.start.value!! > that.start.value!! -> this.start
            that.start.value!! > this.start.value!! -> that.start
            this.start is Limit.Open -> this.start
            that.start is Limit.Open -> that.start
            else -> this.start // They are both closed and equal
        }
        val newEnd = when {
            this.end is Limit.Unbounded -> that.end
            that.end is Limit.Unbounded -> this.end
            this.end.value!! < that.end.value!! -> this.end
            that.end.value!! < this.end.value!! -> that.end
            this.end is Limit.Open -> this.end
            that.end is Limit.Open -> that.end
            else -> this.end // They are both closed and equal
        }
        return if (isEmpty(newStart, newEnd)) null else newStart to newEnd
    }

    /**
     * Checks whether the given value is contained within this range.
     */
    final override operator fun contains(value: T): Boolean = start.isBelow(value) && end.isAbove(value)

    private fun Limit<T>.isAbove(other: T) = when (this) {
        is Limit.Closed -> value >= other
        is Limit.Open -> value > other
        is Limit.Unbounded -> true
    }

    private fun Limit<T>.isBelow(other: T) = when (this) {
        is Limit.Closed -> value <= other
        is Limit.Open -> value < other
        is Limit.Unbounded -> true
    }

    /**
     * Checks whether the range is empty. The range is empty if its start value is greater than the end value for
     * non-exclusive endpoints, or if the start value equals the end value when either endpoint is exclusive.
     */
    private fun isEmpty(start: Limit<T>, end: Limit<T>): Boolean {
        if (start is Limit.Unbounded || end is Limit.Unbounded) return false
        val exclusive = start is Limit.Open || end is Limit.Open
        return if (exclusive) start.value!! >= end.value!! else start.value!! > end.value!!
    }

    final override fun toString(): String {
        val lowerBrace = if (start is Limit.Closed) '[' else '('
        val lowerValue = start.value ?: "  "
        val upperValue = end.value ?: "  "
        val upperBrace = if (end is Limit.Closed) ']' else ')'
        return "$lowerBrace$lowerValue,$upperValue$upperBrace"
    }

    final override fun equals(other: Any?): Boolean {
        return other is ContinuousRange<*> &&
            other.start == start &&
            other.end == end
    }

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + end.hashCode()
        return result
    }
}
