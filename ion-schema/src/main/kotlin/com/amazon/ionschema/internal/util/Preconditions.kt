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

import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
internal inline fun islRequire(value: Boolean, lazyMessage: () -> Any) {
    contract { returns() implies value }
    if (!value) throw InvalidSchemaException(lazyMessage().toString())
}

@OptIn(ExperimentalContracts::class)
internal inline fun <reified T : Any> islRequireNotNull(value: T?, lazyMessage: () -> Any): T {
    contract { returns() implies (value is T) }
    if (value !is T) throw InvalidSchemaException(lazyMessage().toString())
    return value
}

@OptIn(ExperimentalContracts::class)
internal inline fun <reified T : IonValue> islRequireIonNotNull(value: T?, lazyMessage: () -> Any): T {
    contract { returns() implies (value is T) }
    if (value !is T || value.isNullValue) throw InvalidSchemaException(lazyMessage().toString())
    return value
}

@OptIn(ExperimentalContracts::class)
internal inline fun <reified T : IonValue> islRequireIonTypeNotNull(value: IonValue?, lazyMessage: () -> Any): T {
    contract { returns() implies (value is T) }
    if (value !is T || value.isNullValue) throw InvalidSchemaException(lazyMessage().toString())
    return value
}
