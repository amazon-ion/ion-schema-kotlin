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

import software.amazon.ion.IonSymbol
import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.constraint.ConstraintBase
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation

/**
 * Implementation of [Type] representing types identified only by name.
 */
internal class TypeNamed(
        ion: IonSymbol,
        internal val type: TypeInternal
) : TypeInternal by type, ConstraintBase(ion) {

    override fun validate(value: IonValue, issues: Violations) {
        val struct = ion.system.newEmptyStruct()
        struct.put("type", ion.clone())
        val violation = Violation(struct, "type_mismatch")
        type.validate(value, violation)
        if (!violation.isValid()) {
            violation.message = "expected type %s".format(name)
            issues.add(violation)
        }
    }

    override val name = ion.stringValue()
}
