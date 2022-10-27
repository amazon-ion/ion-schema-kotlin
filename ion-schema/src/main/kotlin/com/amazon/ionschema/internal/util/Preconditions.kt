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

import com.amazon.ion.IonContainer
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
 * Validates that the given [IonContainer] has homogeneous elements of Ion type [T].
 *
 * @throws InvalidSchemaException if this [IonContainer] does not meet the above condition.
 * @return the elements of [container] as `List<T>`
 */
internal inline fun <reified T : IonValue> islRequireElementType(container: IonContainer, allowNulls: Boolean = false, lazyMessage: () -> Any): List<T> {
    islRequire(container.all { it is T && (allowNulls || !it.isNullValue) }, lazyMessage)
    return container.filterIsInstance<T>()
}
