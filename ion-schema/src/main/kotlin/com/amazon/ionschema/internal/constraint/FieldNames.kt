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

package com.amazon.ionschema.internal.constraint

import com.amazon.ion.IonStruct
import com.amazon.ion.IonValue
import com.amazon.ionschema.Violation
import com.amazon.ionschema.ViolationChild
import com.amazon.ionschema.Violations
import com.amazon.ionschema.internal.DeferredReferenceManager
import com.amazon.ionschema.internal.SchemaInternal
import com.amazon.ionschema.internal.TypeReference

/**
 * Implements the field_names constraint.
 * This constraint compares the text of all the field names in a struct against an ISL type.
 *
 * @see https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#field_names
 */
internal class FieldNames(ion: IonValue, schema: SchemaInternal, referenceManager: DeferredReferenceManager) : ConstraintBase(ion) {

    private val fieldNameType = TypeReference.create(ion.clone(), schema, referenceManager, isField = true, allowedAnnotations = setOf("distinct"))
    private val requireDistinctValues: Boolean = ion.hasTypeAnnotation("distinct")

    override fun validate(value: IonValue, issues: Violations) {

        validateAs<IonStruct>(value, issues) { v ->

            val fieldNameIssues = Violation(ion, "field_names_mismatch", "field names in struct do not meet expectations")
            val distinctnessIssues = Violation(ion, "field_names_not_distinct", "one or more field names are duplicate values")

            val invalidDuplicates = if (requireDistinctValues) v.map { it.fieldName }.notDistinct() else emptySet()

            // For each field name, check if it matches the type reference and if it is in the set of duplicated field names.
            v.sortedBy { it.fieldName }.forEach {

                val fieldNameAsIonSymbol = it.system.newSymbol(it.fieldNameSymbol)

                // Validate fieldNameAsIonSymbol against fieldNameType
                val fieldNameValidation = ViolationChild(it.fieldName, value = fieldNameAsIonSymbol)
                fieldNameType().validate(fieldNameAsIonSymbol, fieldNameValidation)
                if (!fieldNameValidation.isValid()) {
                    fieldNameIssues.add(fieldNameValidation)
                }

                // Is it a duplicate field name?
                if (it.fieldName in invalidDuplicates) {
                    distinctnessIssues.add(ViolationChild(it.fieldName, value = fieldNameAsIonSymbol))
                }
            }
            if (!fieldNameIssues.isValid()) {
                issues.add(fieldNameIssues)
            }
            if (!distinctnessIssues.isValid()) {
                issues.add(distinctnessIssues)
            }
        }
    }

    /**
     * Helper function to get a set of all elements in the original collection that are duplicated.
     */
    private fun <T> Iterable<T>.notDistinct(): Set<T> = groupingBy { it }.eachCount().filterValues { it > 1 }.keys
}
