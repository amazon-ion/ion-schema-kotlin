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

package com.amazon.ionschema.internal

import com.amazon.ion.IonStruct
import com.amazon.ion.IonValue
import com.amazon.ionschema.Violation
import com.amazon.ionschema.Violations
import com.amazon.ionschema.internal.constraint.ConstraintBase

/**
 * Type implementation instantiated to represent built-in types.
 *
 * @see TypeBuiltin
 */
internal class TypeBuiltinImpl private constructor(
    ion: IonStruct,
    private val delegate: TypeInternal
) : TypeInternal by delegate, ConstraintBase(ion), TypeBuiltin {

    constructor (ionStruct: IonStruct, schema: SchemaInternal) :
        this(ionStruct, TypeImpl(ionStruct, schema, addDefaultTypeConstraint = false))

    override val name: String = ion.fieldName

    override fun validate(value: IonValue, issues: Violations) {
        val struct = ion.system.newEmptyStruct()
        struct.put("type", ion.system.newSymbol(name))
        val violation = Violation(struct, "type_mismatch")
        delegate.validate(value, violation)
        if (!violation.isValid()) {
            violation.message = "expected type %s".format(name)
            issues.add(violation)
        }
    }
}
