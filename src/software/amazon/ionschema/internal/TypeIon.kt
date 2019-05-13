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
import software.amazon.ion.IonType
import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.constraint.ConstraintBase
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation

/**
 * Instantiated to represent individual Ion Types as defined by the
 * Ion Schema Specification.
 */
internal class TypeIon(
        nameSymbol: IonSymbol
    ) : TypeInternal, ConstraintBase(nameSymbol), TypeBuiltin {

    private val ionType = IonType.valueOf(nameSymbol.stringValue().toUpperCase().substring(1))

    override val name = nameSymbol.stringValue()

    override fun getBaseType() = this

    override fun isValidForBaseType(value: IonValue) = ionType.equals(value.type)

    override fun validate(value: IonValue, issues: Violations) {
        if (!ionType.equals(value.type)) {
            issues.add(Violation(ion, "type_mismatch",
                    "expected type %s, found %s".format(
                            ionType.toString().toLowerCase(),
                            value.type.toString().toLowerCase())))
        }
    }
}
