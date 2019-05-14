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

import software.amazon.ion.IonType
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.constraint.ConstraintBase
import software.amazon.ionschema.Violations

/**
 * [Type] decorator that implements the nullable:: annotation.
 */
internal class TypeNullable(
        ion: IonValue,
        private val type: TypeInternal,
        schema: Schema
) : TypeInternal by type, ConstraintBase(ion) {

    init {
        if (type.getBaseType() == schema.getType("document")) {
            throw InvalidSchemaException("The 'document' type is not nullable")
        }
    }

    override fun validate(value: IonValue, issues: Violations) {
        if (!(value.isNullValue
                    && (value.type == IonType.NULL || type.isValidForBaseType(value)))) {
            type.validate(value, issues)
        }
    }

    override val name = type.name
}
