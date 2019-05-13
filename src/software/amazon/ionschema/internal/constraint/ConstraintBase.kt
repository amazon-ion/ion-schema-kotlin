/*
 * Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonValue
import software.amazon.ionschema.Violations
import software.amazon.ionschema.internal.CommonViolations
import software.amazon.ionschema.internal.Constraint

/**
 * Base class for constraint implementations.
 */
internal abstract class ConstraintBase(
        val ion: IonValue
) : Constraint {

    override val name = ion.fieldName

    internal inline fun <reified T> validateAs(value: IonValue, issues: Violations, noinline customValidation: (T) -> Unit)
            = validateAs(T::class.java, value, issues, customValidation)

    internal fun <T> validateAs(expectedClass: Class<T>, value: IonValue, issues: Violations, customValidation: (T) -> Unit) {
        when {
            !expectedClass.isInstance(value) -> issues.add(CommonViolations.INVALID_TYPE(ion, value))
            value.isNullValue -> issues.add(CommonViolations.NULL_VALUE(ion))
            else ->
                @Suppress("UNCHECKED_CAST")
                customValidation(value as T)
        }
    }

    override fun toString() = ion.toString()
}

