package com.amazon.ionschema.model

/**
 * Represents a value that can be an integer or an integer range.
 * Example usage—modelling container length, decimal precision, etc.
 */
class DiscreteRange internal constructor(private val delegate: RangeDelegate<Int>) : Range<Int> by delegate {
    constructor(min: Min, max: Int) : this(RangeDelegate(min, max))
    constructor(min: Int, max: Int) : this(RangeDelegate(min, max))
    constructor(min: Int, max: Max) : this(RangeDelegate(min, max))

    init {
        if (delegate.min is Int && delegate.max is Int) require(delegate.min <= delegate.max)
    }

    override fun hashCode(): Int = delegate.hashCode()
    override fun equals(other: Any?): Boolean = other is DiscreteRange && delegate == other.delegate
    override fun toString(): String = "DiscreteRange(min=$min,max=$max)"
}
