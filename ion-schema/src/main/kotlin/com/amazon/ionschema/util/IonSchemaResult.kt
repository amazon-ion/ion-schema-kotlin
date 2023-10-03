package com.amazon.ionschema.util

import com.amazon.ionschema.IonSchemaException

/**
 * [IonSchemaResult] is a type that represents either success [Ok] or failure [Err].
 */
sealed class IonSchemaResult<T, E : Any> {
    /**
     * Gets the [Ok] result value. Throws an [IonSchemaException] if the result is [Err].
     */
    abstract fun unwrap(): T

    /**
     * Gets the [Ok] value or `null` if not [Ok].
     */
    fun okValueOrNull(): T? = (this as? Ok)?.value

    /**
     * Gets the [Err] value or `null` if not an [Err].
     */
    fun errValueOrNull(): E? = (this as? Err)?.err

    /**
     * Returns `true` if the result is [Ok].
     */
    fun isOk() = this is Ok

    /**
     * Returns `true` if the result is [Err].
     */
    fun isErr() = this is Err

    /**
     * Contains the success value
     */
    data class Ok<T, E : Any>(internal val value: T) : IonSchemaResult<T, E>() {
        override fun unwrap(): T = value
    }

    /**
     * Contains the error value
     */
    data class Err<T, E : Any>(
        val err: E,
        internal var toException: (E) -> IonSchemaException = { IonSchemaException("$it") }
    ) : IonSchemaResult<T, E>() {
        override fun unwrap(): Nothing = throw toException(err)
    }
}
