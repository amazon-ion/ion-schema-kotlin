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

import com.amazon.ion.IonSymbol
import com.amazon.ionschema.internal.constraint.ConstraintBase

/**
 * Implementation of [Type] representing a type imported with an alias.
 */
internal class TypeAliased(
    ion: IonSymbol,
    internal val type: TypeInternal
) : TypeInternal by type, ConstraintBase(ion) {

    override val name = ion.stringValue()
}
