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

package com.amazon.ionschema.internal.util

import com.amazon.ion.IonStruct
import com.amazon.ion.IonValue

/**
 * Returns `this` value, without annotations.
 * If this value has no annotations, returns `this`;
 * otherwise, returns a clone of `this` with the annotations removed.
 */
internal fun IonValue.withoutTypeAnnotations() =
    if (typeAnnotations.isNotEmpty()) {
        clone().apply { clearTypeAnnotations() }
    } else {
        this
    }

/**
 * Gets all fields from a struct that have the given field name.
 */
internal fun IonStruct.getFields(fieldName: String): List<IonValue> {
    return this.filter { it.fieldName == fieldName }
}

/**
 * Makes an IonValue instance read-only.
 */
internal fun <T : IonValue> T.markReadOnly(): T {
    this.makeReadOnly()
    return this
}
