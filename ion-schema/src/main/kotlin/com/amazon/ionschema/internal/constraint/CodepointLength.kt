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

package com.amazon.ionschema.internal.constraint

import com.amazon.ion.IonText
import com.amazon.ion.IonValue

/**
 * Implements the codepoint_length constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#codepoint_length
 */
internal class CodepointLength(
    ion: IonValue
) : ConstraintBaseIntRange<IonText>(IonText::class.java, ion) {

    override val violationCode = "invalid_codepoint_length"
    override val violationMessage = "invalid codepoint length %s, expected %s"

    override fun getIntValue(value: IonText) = value.stringValue().let { it.codePointCount(0, it.length) }
}
