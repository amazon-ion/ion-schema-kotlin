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

import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ionschema.internal.Constraint
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.Schema
import com.amazon.ionschema.internal.constraint.Occurs.Companion.OPTIONAL
import com.amazon.ionschema.ViolationChild
import com.amazon.ionschema.Violations
import com.amazon.ionschema.Violation

/**
 * Implements the fields constraint.
 *
 * [Content] and [Occurs] constraints in the context of a struct are also
 * handled by this class.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#fields
 */
internal class Fields(
        ionValue: IonValue,
        private val schema: Schema
) : ConstraintBase(ionValue), Constraint {

    private val ionStruct: IonStruct
    private val contentConstraintIon: IonValue?
    private val contentClosed: Boolean

    init {
        if (ionValue.isNullValue || ionValue !is IonStruct || ionValue.size() == 0) {
            throw InvalidSchemaException(
                "fields must be a struct that defines at least one field ($ionValue)")
        }
        ionStruct = ionValue
        ionStruct.associateBy(
                { it.fieldName },
                { Occurs(it, schema, OPTIONAL) })

        contentConstraintIon = (ionStruct.container as? IonStruct)?.get("content") as? IonSymbol
        contentClosed = contentConstraintIon?.stringValue().equals("closed")
    }

    override fun validate(value: IonValue, issues: Violations) {
        validateAs<IonStruct>(value, issues) { v ->
            val fieldIssues = Violation(ion, "fields_mismatch", "one or more fields don't match expectations")
            val fieldConstraints = ionStruct.associateBy(
                    { it.fieldName },
                    { Pair(Occurs(it, schema, OPTIONAL, isField = true),
                            ViolationChild(fieldName = it.fieldName))
                    })
            var closedContentIssues: Violation? = null

            v.iterator().forEach {
                val pair = fieldConstraints[it.fieldName]
                if (pair != null) {
                    pair.first.validate(it, pair.second)
                } else if (contentClosed) {
                    if (closedContentIssues == null) {
                        closedContentIssues = Violation(contentConstraintIon,
                                "unexpected_content", "found one or more unexpected fields")
                        issues.add(closedContentIssues!!)
                    }
                    closedContentIssues!!.add(ViolationChild(it.fieldName, value = it))
                }
            }

            fieldConstraints.values.forEach { pair ->
                pair.first.validateAttempts(pair.second)
                if (!pair.second.isValid()) {
                    fieldIssues.add(pair.second)
                }
            }
            if (!fieldIssues.isValid()) {
                issues.add(fieldIssues)
            }
        }
    }
}

