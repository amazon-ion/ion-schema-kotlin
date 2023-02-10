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
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.Violation
import com.amazon.ionschema.ViolationChild
import com.amazon.ionschema.Violations
import com.amazon.ionschema.internal.Constraint
import com.amazon.ionschema.internal.SchemaInternal
import com.amazon.ionschema.internal.constraint.Occurs.Companion.OPTIONAL
import com.amazon.ionschema.internal.util.islRequire
import com.amazon.ionschema.internal.util.islRequireIonTypeNotNull

/**
 * Implements the fields constraint.
 *
 * [Content] and [Occurs] constraints in the context of a struct are also
 * handled by this class.
 *
 * @see https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#fields
 * @see https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#fields
 */
internal class Fields(
    ionValue: IonValue,
    private val schema: SchemaInternal,
) : ConstraintBase(ionValue), Constraint {

    private val ionStruct: IonStruct
    private val contentConstraintIon: IonValue?
    private val contentClosed: Boolean

    private val fieldConstraints: Map<String, Occurs>

    init {
        ionStruct = islRequireIonTypeNotNull(ionValue) { "fields must be a struct that defines at least one field: $ionValue" }
        islRequire(ionStruct.size() != 0) { "fields struct must define at least one field: $ionStruct" }

        val distinctFieldNames = ionStruct.map { it.fieldName }.distinct()
        islRequire(ionStruct.size() == distinctFieldNames.size) { "fields must be a struct with no repeated field names: $ionStruct" }

        if (schema.ionSchemaLanguageVersion >= IonSchemaVersion.v2_0) {
            islRequire(ionStruct.typeAnnotations.all { it == "closed" }) { "Illegal annotation(s) for fields: $ionStruct" }
            islRequire(ionStruct.typeAnnotations.size <= 1) { "fields may have at most one annotation: $ionStruct" }
        }

        // Forces the field definitions to be validated
        fieldConstraints = ionStruct.associateBy(
            { it.fieldName },
            { Occurs(it, schema, OPTIONAL, isField = true) }
        )

        if (schema.ionSchemaLanguageVersion >= IonSchemaVersion.v2_0) {
            contentConstraintIon = ionStruct // In Ion Schema 2.0, the `fields` constraint determines if content is closed.
            contentClosed = ionValue.hasTypeAnnotation("closed")
        } else {
            contentConstraintIon = (ionValue.container as? IonStruct)?.get("content") as? IonSymbol
            contentClosed = contentConstraintIon?.stringValue().equals("closed")
        }
    }

    override fun validate(value: IonValue, issues: Violations) {
        validateAs<IonStruct>(value, issues) { v ->
            val fieldIssues = Violation(ion, "fields_mismatch", "one or more fields don't match expectations")
            val fieldConstraints = this.fieldConstraints.mapValues { (fieldName, occurs) ->
                Pair(
                    occurs.validator(),
                    ViolationChild(fieldName = fieldName)
                )
            }
            var closedContentIssues: Violation? = null

            v.iterator().forEach {
                val pair = fieldConstraints[it.fieldName]
                if (pair != null) {
                    pair.first.validate(it, pair.second)
                } else if (contentClosed) {
                    if (closedContentIssues == null) {
                        closedContentIssues = Violation(
                            contentConstraintIon,
                            "unexpected_content", "found one or more unexpected fields"
                        )
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
