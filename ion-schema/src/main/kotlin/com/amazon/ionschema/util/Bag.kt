package com.amazon.ionschema.util

/**
 * A Bag (also known as a Multiset) is an unordered collection that allows duplicate elements.
 */
class Bag<out E>(elements: List<E>) : Collection<E> by elements {
    constructor() : this(emptyList())

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Bag<*>) return false
        if (size != other.size) return false
        return this.groupingBy { it }.eachCount() == other.groupingBy { it }.eachCount()
    }

    override fun hashCode(): Int = this.sumBy { it.hashCode() }
}
