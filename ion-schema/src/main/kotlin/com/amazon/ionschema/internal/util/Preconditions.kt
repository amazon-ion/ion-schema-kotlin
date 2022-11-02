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

package com.amazon.ionschema.internal.util

import com.amazon.ion.IonStruct
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Checks a condition that is required for a Schema to be valid.
 *
 * @throws InvalidSchemaException if condition evaluates to false.
 */
@OptIn(ExperimentalContracts::class)
internal inline fun islRequire(value: Boolean, lazyMessage: () -> Any) {
    contract { returns() implies value }
    if (!value) throw InvalidSchemaException(lazyMessage().toString())
}

/**
 * Validates that a value is not (Kotlin) `null`.
 *
 * @throws InvalidSchemaException if [value] does not meet the above condition.
 * @return [value]
 */
@OptIn(ExperimentalContracts::class)
internal inline fun <reified T : Any> islRequireNotNull(value: T?, lazyMessage: () -> Any): T {
    contract { returns() implies (value is T) }
    islRequire(value is T, lazyMessage)
    return value
}

/**
 * Validates that an [IonValue] is not `null` or any Ion null.
 *
 * @throws InvalidSchemaException if [value] does not meet the above conditions.
 * @return [value]
 */
@OptIn(ExperimentalContracts::class)
internal inline fun <reified T : IonValue> islRequireIonNotNull(value: T?, lazyMessage: () -> Any): T {
    contract { returns() implies (value is T) }
    islRequire(value is T && !value.isNullValue, lazyMessage)
    return value
}

/**
 * Validates that an [IonValue] is not `null` or any Ion null, and that the value has Ion type [T].
 *
 * @throws InvalidSchemaException if [value] does not meet the above conditions.
 * @return [value] as [T]
 */
@OptIn(ExperimentalContracts::class)
internal inline fun <reified T : IonValue> islRequireIonTypeNotNull(value: IonValue?, lazyMessage: () -> Any): T {
    contract { returns() implies (value is T) }
    islRequire(value is T && !value.isNullValue, lazyMessage)
    return value
}

/**
 * Validates that this [Collection] has zero or one element(s).
 *
 * @throws InvalidSchemaException if this [Collection] does not meet the above condition.
 * @return the only element, or `null`
 */
internal inline fun <T : Any> Collection<T>.islRequireZeroOrOneElements(lazyMessage: () -> Any): T? {
    islRequire(this.size <= 1, lazyMessage)
    return this.singleOrNull()
}

/**
 * Validates that all elements of an [Iterable] are of Ion type [T].
 * If [allowAnnotations] is false, validates that all elements have no annotations.
 * If [allowIonNulls] is false, validates that all elements are not an Ion null value.
 *
 * @throws InvalidSchemaException if any element in the collection does not meet the above conditions.
 * @return the elements of [this] as `Iterable<T>`
 */
internal inline fun <reified T : IonValue> Iterable<IonValue>.islRequireElementType(
    containerDescription: String,
    allowIonNulls: Boolean = false,
    allowAnnotations: Boolean = false,
): Iterable<T> {
    this.onEach {
        if (allowIonNulls) {
            islRequire(it is T) { "$containerDescription elements must be a ${ionTypeDescription<T>()}: $this" }
        } else {
            islRequireIonTypeNotNull<T>(it) { "$containerDescription elements must be a non-null ${ionTypeDescription<T>()}: $this" }
        }
        if (!allowAnnotations) {
            islRequire(it.typeAnnotations.isEmpty()) { "$containerDescription elements may not have annotations: $this" }
        }
    }
    @Suppress("UNCHECKED_CAST")
    return this as Iterable<T>
}

/**
 * Validates that all elements of a [List] are of Ion type [T].
 * If [allowAnnotations] is false, validates that all elements have no annotations.
 * If [allowIonNulls] is false, validates that all elements are not an Ion null value.
 *
 * @throws InvalidSchemaException if any element in the collection does not meet the above conditions.
 * @return the elements of [this] as `List<T>`
 */
internal inline fun <reified T : IonValue> List<IonValue>.islRequireElementType(
    containerDescription: String,
    allowIonNulls: Boolean = false,
    allowAnnotations: Boolean = false,
): List<T> = (this as Iterable<IonValue>).islRequireElementType<T>(
    containerDescription,
    allowIonNulls,
    allowAnnotations,
) as List<T>

/**
 * Gets a required field from an IonStruct, validating for type, nullability, and presence of annotations.
 *
 * Validates that a given field name is present in the struct exactly once, and that the corresponding field value is
 * of type [T].
 * If [allowAnnotations] is false, validates that the field value has no annotations.
 * If [allowIonNulls] is false, validates that the field value is not an Ion null value.
 *
 * @throws InvalidSchemaException if this [IonStruct] does not contain exactly one element with the given field name
 *      or if the element does not meet the required conditions.
 * @return the value for the given field name
 */
internal inline fun <reified T : IonValue> IonStruct.getIslRequiredField(
    fieldName: String,
    allowIonNulls: Boolean = false,
    allowAnnotations: Boolean = false,
): T {
    val theValue = getIslOptionalField<T>(fieldName, allowIonNulls, allowAnnotations)
    return islRequireNotNull(theValue) { "missing required field '$fieldName': $this" }
}

/**
 * Gets an optional field from an IonStruct, validating for type, nullability, and presence of annotations.
 *
 * Validates that a given field name is present in the struct zero or one times, and that the corresponding field value
 * (if it exists) is of type [T].
 * If [allowAnnotations] is false, validates that the field value has no annotations.
 * If [allowIonNulls] is false, validates that the field value is not an Ion null value.
 *
 * If the field name does not occur in the struct, returns (Kotlin) `null`.
 *
 * @throws InvalidSchemaException if this [IonStruct] does not contain zero or one element with the given field name
 *      or if the element does not meet the required conditions.
 * @return the value for the given field name or null if no value has the given field name
 */
internal inline fun <reified T : IonValue> IonStruct.getIslOptionalField(
    fieldName: String,
    allowIonNulls: Boolean = false,
    allowAnnotations: Boolean = false,
): T? {
    if (!this.containsKey(fieldName)) return null
    val theValue = this.getFields(fieldName)
        .also { islRequire(it.size == 1) { "field '$fieldName' may not occur more than once: $this" } }
        .single()
    if (allowIonNulls) {
        islRequire(theValue is T) { "field must be a ${ionTypeDescription<T>()}; '$fieldName': $theValue" }
    } else {
        islRequireIonTypeNotNull<T>(theValue) { "field must be a non-null ${ionTypeDescription<T>()}; '$fieldName': $theValue" }
    }
    if (!allowAnnotations) {
        islRequire(theValue.typeAnnotations.isEmpty()) { "field may not have annotations; '$fieldName': $theValue" }
    }
    return theValue
}

/**
 * Converts an interface in the [IonValue] hierarchy into a prose-friendly name.
 */
private inline fun <reified T : IonValue> ionTypeDescription(): String {
    return when (val name = T::class.simpleName?.removePrefix("Ion")?.toLowerCase()) {
        null -> TODO("Unreachable")
        "number" -> "int, float, or decimal"
        "text" -> "string or symbol"
        "sequence" -> "list or sexp"
        "container" -> "struct, list, or sexp"
        "lob" -> "blob or clob"
        else -> name
    }
}

/**
 * Validates that an [IonStruct] contains no unexpected fields.
 * @return [this]
 * @throws InvalidSchemaException if any fields have a name not in [expectedFieldNames].
 */
internal fun IonStruct.islRequireOnlyExpectedFieldNames(
    expectedFieldNames: Collection<String>,
): IonStruct {
    val unknownFields = this.filterNot { it.fieldName in expectedFieldNames }
    if (unknownFields.isNotEmpty()) {
        throw InvalidSchemaException("Unknown fields ${unknownFields.map { it.fieldName }} in struct: $this")
    } else {
        return this
    }
}
