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

package com.amazon.ionschema.internal

import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.Violation
import com.amazon.ionschema.Violations
import com.amazon.ionschema.internal.IonSchemaSystemImpl.Param.SHORT_CIRCUIT_ON_INVALID_ANNOTATIONS
import com.amazon.ionschema.internal.constraint.ConstraintBase
import com.amazon.ionschema.internal.util.markReadOnly

/**
 * Implementation of [TypeInternal] backed by a collection of zero or more [Constraint]s.
 *
 * If no "type" constraint is found, the default "type: any" is inserted by this class
 * (unless addDefaultTypeConstraint is `false`).
 */
internal class TypeImpl(
    private val ionStruct: IonStruct,
    private val schema: SchemaInternal,
    referenceManager: DeferredReferenceManager,
    addDefaultTypeConstraint: Boolean = true
) : TypeInternal, ConstraintBase(ionStruct) {

    private companion object {
        private val ION = IonSystemBuilder.standard().build()
        private val ANY = ION.newSymbol("any")

        /**
         * Order in which constraints should be evaluated. Lower is first.
         *
         * The [validate] function in this class has optional short-circuit logic for the `annotations` constraint, so
         * `annotations` gets special treatment and will be evaluated first.
         */
        private val CONSTRAINT_EVALUATION_ORDER = mapOf(
            "annotations" to -1,
            // By default, all constraints are priority 0
        )
        private val CONSTRAINT_PRIORITY_COMPARATOR = Comparator<Constraint> {
            a, b ->
            CONSTRAINT_EVALUATION_ORDER.getOrDefault(a.name, 0)
                .compareTo(CONSTRAINT_EVALUATION_ORDER.getOrDefault(b.name, 0))
        }
    }

    override val isl = ionStruct.markReadOnly()

    internal val constraints: List<Constraint>

    init {
        var foundTypeConstraint = false
        constraints = ionStruct.asSequence()
            .filter { it.fieldName == null || schema.getSchemaSystem().isConstraint(it.fieldName, schema) }
            .onEach { if (it.fieldName == "type") { foundTypeConstraint = true } }
            .map { schema.getSchemaSystem().constraintFor(it, schema, referenceManager) }
            .toMutableList()

        if (schema.ionSchemaLanguageVersion == IonSchemaVersion.v1_0) {
            if (!foundTypeConstraint && addDefaultTypeConstraint) {
                // default type for ISL 1.0 is 'any':
                constraints.add(TypeReference.create(ANY, schema, referenceManager)())
            }
        }

        constraints.sortWith(CONSTRAINT_PRIORITY_COMPARATOR)

        if (schema is SchemaImpl_2_0) schema.validateFieldNamesInType(ionStruct)
    }

    override val name = (ionStruct.get("name") as? IonSymbol)?.stringValue() ?: ionStruct.toString()

    override val schemaId: String? = schema.schemaId

    @Deprecated("Only used for Ion Schema 1.0 code paths. No new usages should be introduced.")
    override fun getBaseType(): TypeBuiltin {
        val type = ionStruct["type"]
        if (type != null && type is IonSymbol) {
            val parentType = schema.getType(type.stringValue())
            if (parentType != null) {
                return parentType.getBaseType()
            }
        }
        return BuiltInTypes["any"]!!
    }

    @Deprecated("Only used for Ion Schema 1.0 code paths. No new usages should be introduced.")
    override fun isValidForBaseType(value: IonValue) = getBaseType().isValidForBaseType(value)

    override fun validate(value: IonValue, issues: Violations) {
        val constraintIterator = constraints.iterator()
        val typeWantsToAcceptNull = isl.get("type")?.hasTypeAnnotation("\$null_or") == true
        val incompatibleConstraintsWithNullOrIssues = Violation(
            isl,
            "constraints_incompatible",
            "type cannot accept null. note: type attempts to accept null via \$null_or but defines one or more constraints for which null are never valid - did you mean to use \$null_or on the type definition itself?"
        )

        fun validateConstraintAndSeparateNullViolations(constraint: Constraint) {
            val constraintIssues = Violations()
            val constraintNotApplicableForNullViolation = Violation(message = "constraint \"${constraint.name}\" is not applicable for null values")
            constraint.validate(value, constraintIssues)
            constraintIssues.forEach {
                if (it.code == "null_value") {
                    incompatibleConstraintsWithNullOrIssues.add(constraintNotApplicableForNullViolation)
                } else {
                    issues.add(it)
                }
            }
        }

        // Handle short-circuit returns for `annotations`, if enabled
        if (schema.getSchemaSystem().getParam(SHORT_CIRCUIT_ON_INVALID_ANNOTATIONS)) {
            // To avoid adding unnecessary branches for all the constraints that are not "annotations",
            // this short circuit logic will run until there is a constraint that is not named "annotations"
            // and then fall back to the default logic for the remaining constraints.
            while (constraintIterator.hasNext()) {
                val c = constraintIterator.next()
                if (c.name == "annotations") {
                    val checkpoint = issues.checkpoint()
                    c.validate(value, issues)
                    if (!checkpoint.isValid()) return
                } else {
                    // No more "annotations", so handle normally and then exit the loop
                    if (typeWantsToAcceptNull) {
                        validateConstraintAndSeparateNullViolations(c)
                    } else {
                        c.validate(value, issues)
                    }
                    break
                }
            }
        }

        if (typeWantsToAcceptNull) {
            // If this type wants to accept null, buffer in the violations for this type so we can check if any of them
            // are a null_value violation. If there are null_value violations, we can present a more helpful error to
            // the user.
            constraintIterator.forEach {
                validateConstraintAndSeparateNullViolations(it)
            }

            if (!incompatibleConstraintsWithNullOrIssues.isValid()) {
                issues.add(incompatibleConstraintsWithNullOrIssues)
            }
        } else {
            // We do not need to buffer the violations into another list if the type definition doesn't use $null_or.
            constraintIterator.forEach {
                it.validate(value, issues)
            }
        }
    }
}
