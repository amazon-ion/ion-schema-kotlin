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

import com.amazon.ion.IonContainer
import com.amazon.ion.IonValue
import com.amazon.ionschema.IonSchemaVersion.v2_0
import com.amazon.ionschema.Schema
import com.amazon.ionschema.Violation
import com.amazon.ionschema.ViolationChild
import com.amazon.ionschema.Violations
import com.amazon.ionschema.internal.TypeReference
import com.amazon.ionschema.internal.TypeReference.Companion.DEFAULT_ALLOWED_ANNOTATIONS
import com.amazon.ionschema.internal.util.islRequire

/**
 * Implements the element constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/isl-1-0/spec#element
 * @see https://amzn.github.io/ion-schema/docs/isl-2-0/spec#element
 */
internal class Element(
    ion: IonValue,
    schema: Schema
) : ConstraintBase(ion) {

    private val typeReference = TypeReference.create(ion, schema, isField = true, allowedAnnotations = DEFAULT_ALLOWED_ANNOTATIONS + "distinct")
    private val requireDistinctValues: Boolean

    init {
        requireDistinctValues = if (ion.hasTypeAnnotation("distinct")) {
            islRequire(schema.ionSchemaLanguageVersion >= v2_0) { "The 'distinct' elements annotation is not supported before Ion Schema 2.0" }
            true
        } else {
            false
        }
    }

    override fun validate(value: IonValue, issues: Violations) {
        validateAs<IonContainer>(value, issues) { v ->
            val elementIssues = Violation(ion, "element_mismatch", "one or more elements don't match expectations")

            val distinctnessIssues = Violation(ion, "element_not_distinct", "one or more elements are duplicate values")
            val invalidDuplicates = if (requireDistinctValues) v.notDistinct() else emptySet()

            // For each element, check if it matches the type reference and if it is in the set of duplicated elements.
            v.forEachIndexed { idx, it ->
                val elementValidation = violationChild(idx, it)
                typeReference().validate(it, elementValidation)
                if (!elementValidation.isValid()) {
                    elementIssues.add(elementValidation)
                }
                if (requireDistinctValues && it in invalidDuplicates) {
                    distinctnessIssues.add(violationChild(idx, it))
                }
            }
            if (!elementIssues.isValid()) {
                issues.add(elementIssues)
            }
            if (!distinctnessIssues.isValid()) {
                issues.add(distinctnessIssues)
            }
        }
    }

    private fun <T> Iterable<T>.notDistinct(): Set<T> = groupingBy { it }.eachCount().filterValues { it > 1 }.keys

    private fun violationChild(idx: Int, it: IonValue) =
        if (it.fieldName == null) {
            ViolationChild(index = idx, value = it)
        } else {
            ViolationChild(fieldName = it.fieldName, value = it)
        }
}
