package com.amazon.ionschema.util

private val EMPTY_BAG: Bag<Nothing> = Bag(emptyList())

/**
 * Returns an empty read-only bag.
 */
fun <E> emptyBag(): Bag<E> = EMPTY_BAG

/**
 * Returns a new read-only bag of given elements.
 */
fun <E> bagOf(vararg values: E): Bag<E> = if (values.isEmpty()) emptyBag() else Bag(values.toList())

/**
 * Returns a [Bag] containing all elements.
 */
fun <E> Iterable<E>.toBag(): Bag<E> = Bag(this.toList())
