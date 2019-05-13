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

package software.amazon.ionschema.internal

import software.amazon.ion.IonStruct
import software.amazon.ion.IonValue
import software.amazon.ionschema.Schema
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation
import software.amazon.ionschema.internal.constraint.ConstraintBase

/**
 * Implementation of [Type] corresponding to inline type definitions.
 */
internal class TypeInline private constructor (
        ion: IonStruct,
        private val type: TypeInternal
) : ConstraintBase(ion), TypeInternal by type {

    constructor(ionStruct: IonStruct, schema: Schema)
            : this(ionStruct, TypeImpl(ionStruct, schema))

    override val name = type.name

    override fun validate(value: IonValue, issues: Violations) {
        val violation = Violation(ion, "type_mismatch")
        type.validate(value, violation)
        if (!violation.isValid()) {
            violation.message = "expected type %s".format(name)
            issues.add(violation)
        }
    }
}

