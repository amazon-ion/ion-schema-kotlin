/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package com.amazon.ionschema.model.constraints

import com.amazon.ionelement.api.IonElement
import com.amazon.ionschema.model.AstConstraint
import com.amazon.ionschema.model.ConstraintId

data class ContainsConstraint(val values: Collection<IonElement>) : AstConstraint<ContainsConstraint> {
    companion object : ConstraintId<ContainsConstraint> by ConstraintId("contains") {
        @JvmField val ID = this@Companion
    }
    override val id get() = ID
}
