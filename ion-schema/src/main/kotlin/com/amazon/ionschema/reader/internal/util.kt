package com.amazon.ionschema.reader.internal

import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.model.ExperimentalIonSchemaModel

/**
 * Helper function to catch exceptions and convert them into [ReadError] or rethrow if they are a fast-fail exception.
 */
@ExperimentalIonSchemaModel
internal inline fun <T : Any> readCatching(context: ReaderContext, value: IonValue, block: () -> T?): T? {
    return try {
        block()
    } catch (e: Exception) {
        if (e is InvalidSchemaException && e.isFailFast()) {
            throw e
        } else {
            context.reportError(ReadError(value, e.message ?: e.toString()))
        }
        null
    }
}

/**
 * Helper function to map a sequence of items, catching exceptions and convert them into [ReadError].
 */
@ExperimentalIonSchemaModel
internal inline fun <T : Any> Iterable<IonValue>.readAllCatching(context: ReaderContext, crossinline block: (IonValue) -> T?): List<T> {
    return mapNotNull { readCatching(context, it) { block(it) } }
}

/**
 * Formats a message for an invalid constraint
 */
internal fun invalidConstraint(value: IonValue, reason: String, constraintName: String = value.fieldName): String {
    return "Illegal argument for '$constraintName' constraint; $reason: $value"
}
