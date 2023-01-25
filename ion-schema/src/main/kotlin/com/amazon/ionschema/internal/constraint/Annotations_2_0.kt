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

import com.amazon.ion.IonDatagram
import com.amazon.ion.IonList
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ionschema.Schema
import com.amazon.ionschema.Violation
import com.amazon.ionschema.Violations
import com.amazon.ionschema.internal.CommonViolations
import com.amazon.ionschema.internal.TypeInternal
import com.amazon.ionschema.internal.TypeReference
import com.amazon.ionschema.internal.util.islRequire
import com.amazon.ionschema.internal.util.islRequireIonNotNull
import com.amazon.ionschema.internal.util.islRequireIonTypeNotNull

/**
 * Implements the annotations constraint.
 *
 * @see https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#annotations
 */
internal class Annotations_2_0 constructor(
    ion: IonValue,
    schema: Schema,
) : ConstraintBase(ion) {

    private val type: () -> TypeInternal

    companion object {
        private val ALLOWED_MODIFIERS = setOf("closed", "required")
    }

    init {
        type = if (ion is IonList) {
            // This is the "simplified" syntax. In order to keep the validation from being complex, we convert
            // from the "simplified" syntax to the standard syntax.

            islRequireIonNotNull(ion) { "annotations list may not be null" }
            islRequire(ion.typeAnnotations.isNotEmpty() && ion.typeAnnotations.all { it in ALLOWED_MODIFIERS }) {
                "annotations list must be annotated only with one or both of 'closed', 'required'"
            }

            ion.onEach {
                islRequireIonTypeNotNull<IonSymbol>(it) { "annotations list values must be non-null symbols" }
                islRequire(it.typeAnnotations.isEmpty()) { "annotations list values may not be annotated" }
            }

            val annotationListTypeIon = ion.system.newEmptyStruct().apply {
                if (ion.hasTypeAnnotation("closed")) {
                    // May only contain...
                    add(
                        "element",
                        ion.system.newEmptyStruct().apply {
                            add("valid_values", ion.clone().apply { clearTypeAnnotations() })
                        }
                    )
                }
                if (ion.hasTypeAnnotation("required")) {
                    // Must contain...
                    add("contains", ion.clone().apply { clearTypeAnnotations() })
                }
            }

            TypeReference.create(annotationListTypeIon, schema)
        } else {
            TypeReference.create(ion, schema, isField = true)
        }
    }

    override fun validate(value: IonValue, issues: Violations) {
        // Datagrams are always invalid for the `annotations` constraint because they don't have a list of annotations
        if (value is IonDatagram) {
            issues.add(CommonViolations.INVALID_TYPE(ion, value))
            return
        }
        val annotationIssues = Violation(ion, "invalid_annotations", "annotations on value do not meet expectations")
        type().validate(value.typeAnnotations.toIonSymbolList(), annotationIssues)
        if (!annotationIssues.isValid()) {
            issues.add(annotationIssues)
        }
    }

    /**
     * Helper function for taking a List or Array of [String] and turning it into an [IonList] of [IonSymbol]
     */
    private fun Array<String>.toIonSymbolList(): IonList {
        val theSymbols = this.map { ion.system.newSymbol(it) }
        return ion.system.newEmptyList().apply { addAll(theSymbols) }
    }
}
