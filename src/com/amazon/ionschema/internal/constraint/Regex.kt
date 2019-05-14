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

import com.amazon.ion.IonString
import com.amazon.ion.IonText
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.Violations
import com.amazon.ionschema.Violation
import javax.script.ScriptEngineManager

/**
 * Implements the regex constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#regex
 */
internal class Regex(
        ion: IonValue
) : ConstraintBase(ion) {

    companion object {
        private val scriptEngine = ScriptEngineManager().getEngineByName("javascript")
    }

    private val regex = if (ion !is IonString || ion.isNullValue) {
            throw InvalidSchemaException("Invalid regex constraint: $ion")
        } else {
            ion.stringValue()
                    .replace("/", "\\/")      // escape '/'
        }

    private val flags: String

    init {
        val sb = StringBuffer()
        ion.typeAnnotations.forEach {
            when (it) {
                "i", "m" -> sb.append(it)
                else -> throw InvalidSchemaException(
                        "Unrecognized flags for regex ($ion)")
            }
        }
        flags = sb.toString()
    }

    override fun validate(value: IonValue, issues: Violations) {
        validateAs<IonText>(value, issues) { v ->
            val string = v.stringValue()
                    .replace("\"", "\\\"")    // escape '"'
                    .replace("\n", "\\n")     // escape '\n' (new line)
                    .replace("\r", "\\r")     // escape '\r' (carriage return)
            val expr = "(/$regex/$flags).test(\"$string\")"
            val result = scriptEngine.eval(expr) as Boolean

            if (!result) {
                issues.add(Violation(ion, "regex_mismatch", "value doesn't match regex"))
            }
        }
    }
}

