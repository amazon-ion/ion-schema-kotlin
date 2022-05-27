/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package com.amazon.ionschema.model

/**
 * Set of tools for creating a variety of implementations of ranges.
 *
 * * Endpoints of the range do not need to be `T`—just comparable to `T`.
 * * Endpoints of the range can be open (exclusive) or closed (inclusive) using [Boundary].
 * * Ranges can be bounded, left bounded, or right bounded. See [Min] and [Max].
 *
 */
interface Range<T : Comparable<T>> {
    val min: Comparable<T>
    val max: Comparable<T>
    operator fun contains(value: T): Boolean
}

internal class RangeDelegate<T : Comparable<T>>(
    override val min: Comparable<T>,
    override val max: Comparable<T>,
) : Range<T> {

    override operator fun contains(value: T): Boolean {
        val inLower = if (min is Value && min.exclusive) min < value else min <= value
        val inUpper = if (max is Value && max.exclusive) max > value else max >= value
        return inLower && inUpper
    }

    override fun hashCode(): Int = min.hashCode() + 31 * max.hashCode()
    override fun equals(other: Any?): Boolean = other is Range<*> && this.min == other.min && this.max == other.max
}

sealed class Boundary<T> : Comparable<T>

/**
 * Special value that can be compared to anything and that is always greater than the other value.
 */
object Max : Boundary<Any>(), Comparable<Any> {
    override fun compareTo(other: Any): Int = 1
    override fun toString(): String = "Max"
}

/**
 * Special value that can be compared to anything and that is always less than the other value.
 */
object Min : Boundary<Any>(), Comparable<Any> {
    override fun compareTo(other: Any): Int = -1
    override fun toString(): String = "Min"
}

/**
 * A wrapper for endpoint values that allows us to create exclusive endpoints.
 */
class Value<T : Comparable<T>>(val value: T, val exclusive: Boolean = false) : Comparable<T> by value, Boundary<T>()
