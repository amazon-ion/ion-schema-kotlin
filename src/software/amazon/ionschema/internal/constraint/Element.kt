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

package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonContainer
import software.amazon.ion.IonValue
import software.amazon.ionschema.Schema
import software.amazon.ionschema.ViolationChild
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation
import software.amazon.ionschema.internal.TypeReference

/**
 * Implements the element constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#element
 */
internal class Element(
        ion: IonValue,
        schema: Schema
) : ConstraintBase(ion) {

    private val typeReference = TypeReference.create(ion, schema, isField = true)

    override fun validate(value: IonValue, issues: Violations) {
        validateAs<IonContainer>(value, issues) { v ->
            val elementIssues = Violation(ion, "element_mismatch", "one or more elements don't match expectations")
            v.forEachIndexed { idx, it ->
                val elementValidation = if (it.fieldName == null) {
                        ViolationChild(index = idx, value = it)
                    } else {
                        ViolationChild(fieldName = it.fieldName, value = it)
                    }
                typeReference().validate(it, elementValidation)
                if (!elementValidation.isValid()) {
                    elementIssues.add(elementValidation)
                }
            }
            if (!elementIssues.isValid()) {
                issues.add(elementIssues)
            }
        }
    }
}

