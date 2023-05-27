package com.amazon.ionschema.reader.internal

import com.amazon.ion.IonStruct
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.internal.util.IonSchema_2_0
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

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

@OptIn(ExperimentalContracts::class)
internal fun isHeader(value: IonValue): Boolean {
    contract { returns(true) implies (value is IonStruct) }
    return value is IonStruct && !value.isNullValue && arrayOf("schema_header").contentDeepEquals(value.typeAnnotations)
}

@OptIn(ExperimentalContracts::class)
internal fun isFooter(value: IonValue): Boolean {
    contract { returns(true) implies (value is IonStruct) }
    return value is IonStruct && !value.isNullValue && arrayOf("schema_footer").contentDeepEquals(value.typeAnnotations)
}

@OptIn(ExperimentalContracts::class)
internal fun isType(value: IonValue): Boolean {
    contract { returns(true) implies (value is IonStruct) }
    return value is IonStruct && !value.isNullValue && arrayOf("type").contentDeepEquals(value.typeAnnotations)
}

/**
 * Checks whether a given value is allowed as top-level open content.
 * See https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#open-content
 */
internal fun isTopLevelOpenContent(value: IonValue): Boolean {
    if (IonSchemaVersion.isVersionMarker(value)) {
        return false
    }
    if (value.typeAnnotations.any { IonSchema_2_0.RESERVED_WORDS_REGEX.matches(it) }) {
        return false
    }
    return true
}
