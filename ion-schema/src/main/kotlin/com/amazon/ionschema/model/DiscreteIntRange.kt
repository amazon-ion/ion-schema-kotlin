package com.amazon.ionschema.model

/**
 * A range over a type that is a finite or countably infinite set. The values contained in the range include both of the
 * limits—[start] and [endInclusive].
 *
 * A `DiscreteRange` is a _bounded, closed interval_ when both limits are non-null, and it is a _half-bounded interval_
 * when one of the limits is `null`. At least one of [start] and [endInclusive] must be non-null.
 *
 * A `DiscreteRange` is allowed to be a _degenerate interval_ (i.e. `start == endInclusive`), but it may not be an
 * empty interval (i.e. `start > endInclusive`).
 */
class DiscreteIntRange private constructor(private val delegate: ContinuousRange<Int>) {

    // Because we never construct Open bounds, we cannot accidentally create an empty range.
    constructor(start: Int?, endInclusive: Int?) : this(
        ContinuousRange(
            start?.let { ContinuousRange.Limit.Closed(it) } ?: ContinuousRange.Limit.Unbounded,
            endInclusive?.let { ContinuousRange.Limit.Closed(it) } ?: ContinuousRange.Limit.Unbounded
        )
    )

    constructor(value: Int) : this(value, value)

    val start: Int?
        get() = (delegate.start as? ContinuousRange.Limit.Closed)?.value

    val endInclusive: Int?
        get() = (delegate.end as? ContinuousRange.Limit.Closed)?.value

    /**
     * Negates the boundaries of the range, and swaps their position to keep the lower number in the `start` position.
     *
     * For example:
     * ```
     * val range1 = DiscreteIntRange(-2, 10)
     * val range2 = DiscreteIntRange(-10, 2)
     * assertEquals(range1.negate(), range2)
     * ```
     */
    fun negate() = DiscreteIntRange(delegate.end.value?.let { it * -1 }, delegate.start.value?.let { it * -1 })
    fun intersect(other: DiscreteIntRange): DiscreteIntRange? = delegate.intersect(other.delegate)?.let { DiscreteIntRange(it) }

    operator fun contains(value: Int): Boolean = delegate.contains(value)
    override fun toString() = delegate.toString()

    override fun hashCode(): Int = delegate.hashCode()
    override fun equals(other: Any?): Boolean = other is DiscreteIntRange && other.delegate == this.delegate

    operator fun component1() = start
    operator fun component2() = endInclusive
}
