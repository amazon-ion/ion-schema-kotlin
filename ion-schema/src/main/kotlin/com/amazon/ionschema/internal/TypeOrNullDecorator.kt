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

import com.amazon.ion.IonType
import com.amazon.ion.IonValue
import com.amazon.ionschema.IonSchemaVersion.v2_0
import com.amazon.ionschema.Schema
import com.amazon.ionschema.Violations
import com.amazon.ionschema.internal.constraint.ConstraintBase
import com.amazon.ionschema.internal.util.islRequire

/**
 * [Type] decorator that implements the $null_or:: annotation.
 *
 * Name of this class is `TypeOrNull...` instead of `NullOrType...` to be consistent with other Type-based classes.
 */
internal class TypeOrNullDecorator(
    ion: IonValue,
    private val type: TypeInternal,
    schema: Schema
) : TypeInternal by type, ConstraintBase(ion) {

    init {
        islRequire(schema.ionSchemaLanguageVersion >= v2_0) { "'\$null_or::' not supported before Ion Schema 2.0" }
    }

    override fun validate(value: IonValue, issues: Violations) {
        if (value.type == IonType.NULL) {
            return
        } else {
            type.validate(value, issues)
        }
    }

    override val name = type.name
}
