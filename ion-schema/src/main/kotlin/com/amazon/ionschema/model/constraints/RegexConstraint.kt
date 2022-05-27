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

import com.amazon.ionschema.model.AstConstraint
import com.amazon.ionschema.model.ConstraintId

data class RegexConstraint constructor(val pattern: String, val options: Set<Options>) : AstConstraint<RegexConstraint> {
    companion object : ConstraintId<RegexConstraint> by ConstraintId("regex") {
        @JvmField val ID = this@Companion
    }
    override val id get() = ID

    init {
        // Validate the regex pattern
    }

    val regex by lazy { Regex(pattern, options = options.map { it.regexOption }.toSet()) }

    enum class Options(internal val regexOption: RegexOption) {
        MULTILINE(RegexOption.MULTILINE),
        IGNORE_CASE(RegexOption.IGNORE_CASE);
    }
}
