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

package com.amazon.ionschema

import com.amazon.ion.IonValue
import com.amazon.ionschema.internal.Constraint

/**
 * A Type consists of an optional name and zero or more constraints.
 *
 * Unless otherwise specified, the constraint `type: any` is automatically applied.
 */
interface Type {
    /**
     * The name of the type;  if the type has no name, a string representing
     * the definition of the type is returned.
     */
    val name: String

    /**
     * A read-only view of the ISL for this type.
     */
    val isl: IonValue

    /**
     * If the specified value violates one or more of this type's constraints,
     * returns `false`, otherwise `true`.
     */
    fun isValid(value: IonValue): Boolean = validate(this, value, true).isValid()

    /**
     * Returns a Violations object indicating whether the specified value
     * is valid for this type, and if not, provides details as to which
     * constraints were violated.
     */
    fun validate(value: IonValue): Violations = validate(this, value, false)

    private fun validate(type: Type, value: IonValue, shortCircuit: Boolean): Violations {
        val violations = Violations(
                shortCircuit = shortCircuit,
                childrenAllowed = false)
        try {
            (type as Constraint).validate(value, violations)
        } catch (e: ShortCircuitValidationException) {
            // short-circuit validation winds up here, safe to ignore
        }
        return violations
    }
}

