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
@file:JvmName("IonSchemaBuilders")
package com.amazon.ionschema.model

import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.ionSymbol
import com.amazon.ionschema.model.constraints.*
import java.math.BigDecimal

// This file contains a straw-man example of a dsl for creating Ion Schemas.
// Ideally, this would be exposed as java builders and a kotlin type-safe DSL, like
// the ones in the AWS Kotlin SDK.
//
// At the bottom of this file, you can find an example of programmatically constructing the
// types found at https://amzn.github.io/ion-schema/docs/spec.html#customer-profile-data

fun inline(vararg constraints: AstConstraint<*>) = AstType.TypeDefinition(constraints.toList())
fun inline(vararg constraints: AstConstraint<*>, occurs: DiscreteRange) = AstVariablyOccurringType(occurs, inline(*constraints))
fun named(typeId: String) = AstType.TypeReference(typeId)
fun named(typeId: String, occurs: DiscreteRange) = AstVariablyOccurringType(occurs, named(typeId))
fun import(schemaId: String, typeId: String) = AstType.TypeReference(typeId, schemaId)
fun import(occurs: DiscreteRange, schemaId: String, typeId: String) = AstVariablyOccurringType(occurs, AstType.TypeReference(typeId, schemaId))

// Constraints
fun allOf(vararg types: AstType) = AllOfConstraint(types.toList())
fun allOf(types: Iterable<AstType>) = AllOfConstraint(types)
fun annotations(type: AstType) = AnnotationsConstraint(type)
fun anyOf(vararg types: AstType) = AnyOfConstraint(types.toList())
fun anyOf(types: Iterable<AstType>) = AnyOfConstraint(types)
fun byteLength(range: DiscreteRange) = ByteLengthConstraint(range)
fun byteLength(value: Int) = ByteLengthConstraint(DiscreteRange(value, value))
fun codepointLength(range: DiscreteRange) = CodepointLengthConstraint(range)
fun codepointLength(value: Int) = CodepointLengthConstraint(DiscreteRange(value, value))
fun containerLength(range: DiscreteRange) = ContainerLengthConstraint(range)
fun containerLength(value: Int) = ContainerLengthConstraint(DiscreteRange(value, value))
fun contains(vararg values: IonElement) = ContainsConstraint(values.toList())
fun contains(values: Collection<IonElement>) = ContainsConstraint(values)
fun element(type: AstType) = ElementConstraint(type)
fun fields(vararg fields: Pair<String, AstVariablyOccurringType>) = FieldsConstraint(fields.toMap())
fun fields(fields: Map<String, AstVariablyOccurringType>) = FieldsConstraint(fields)
fun not(type: AstType) = NotConstraint(type)
fun oneOf(vararg types: AstType) = OneOfConstraint(types.toList())
fun oneOf(types: Iterable<AstType>) = OneOfConstraint(types)
fun orderedElements(vararg types: AstVariablyOccurringType) = OrderedElementsConstraint(types.toList())
fun orderedElements(types: Iterable<AstVariablyOccurringType>) = OrderedElementsConstraint(types)
fun precision(range: DiscreteRange) = PrecisionConstraint(range)
fun precision(value: Int) = PrecisionConstraint(DiscreteRange(value, value))
fun regex(pattern: String, options: Set<RegexConstraint.Options>) = RegexConstraint(pattern, options)
fun regex(pattern: String, vararg options: RegexConstraint.Options) = RegexConstraint(pattern, options.toSet())
fun scale(range: DiscreteRange) = ScaleConstraint(range)
fun scale(value: Int) = ScaleConstraint(DiscreteRange(value, value))
fun timestampPrecision(range: TimestampPrecisionRange) = TimestampPrecisionConstraint(range)
fun timestampPrecision(value: TimestampPrecisions) = TimestampPrecisionConstraint(TimestampPrecisionRange(value, value))
fun timestampOffset(offset: TimestampOffsetConstraint.Offset) = TimestampOffsetConstraint(offset)
fun timestampOffset(hours: Int, minutes: Int = 0) = TimestampOffsetConstraint(TimestampOffsetConstraint.Offset.Minutes(hours * 60 + minutes))
fun timestampOffset(minutes: Int) = TimestampOffsetConstraint(TimestampOffsetConstraint.Offset.Minutes(minutes))
fun type(type: AstType) = TypeConstraint(type)
fun utf8ByteLength(range: DiscreteRange) = Utf8ByteLengthConstraint(range)
fun utf8ByteLength(value: Int) = Utf8ByteLengthConstraint(DiscreteRange(value, value))
fun validValues(values: Iterable<ValidValue>) = ValidValuesConstraint(values)
fun validValues(vararg values: ValidValue) = ValidValuesConstraint(values.toList())
fun validValues(vararg ionValues: IonElement) = ValidValuesConstraint(ionValues.map { ValidValue.SingleValue(it) })

// Ranges
fun range(min: Int, max: Int) = DiscreteRange(min, max)
fun atLeast(min: Int) = DiscreteRange(min, Max)
fun atMost(max: Int) = DiscreteRange(Min, max)
fun exactly(n: Int) = DiscreteRange(n, n)

// Syntactical sugar
fun nullOr(type: AstType): AstType = inline(anyOf(type, named("\$null")))
fun literal(element: IonElement) = inline(validValues(element))

fun optional(type: AstType) = type.occurs(range(0, 1))
fun required(type: AstType) = type.occurs(exactly(1))
fun AstType.occurs(range: DiscreteRange) = AstVariablyOccurringType(range, this)

// ISL 1.0-ish syntax for annotations
fun annotations(vararg annotationSymbols: CharSequence, closed: Boolean = false, required: Boolean = false): AnnotationsConstraint {
    // TODO: make sure this is correct
    val constraints = mutableListOf<AstConstraint<*>>()
    if (closed) {
        val annotationSymbolValues = annotationSymbols.map {
            ValidValue.SingleValue(ionSymbol(it.toString()))
        }
        constraints.add(element(inline(validValues(annotationSymbolValues))))
    }
    val requiredSymbols = if (required) {
        annotationSymbols.filter { it !is OptionalAnnotation }.map { ionSymbol(it.toString()) }
    } else {
        annotationSymbols.filterIsInstance<RequiredAnnotation>().map { ionSymbol(it.toString()) }
    }
    if (requiredSymbols.isNotEmpty()) {
        constraints.add(contains(requiredSymbols))
    }

    return AnnotationsConstraint(AstType.TypeDefinition(constraints.toList()))
}
internal class RequiredAnnotation(val s: String) : CharSequence by s
internal class OptionalAnnotation(val s: String) : CharSequence by s
fun required(s: String): CharSequence = RequiredAnnotation(s)
fun optional(s: String): CharSequence = OptionalAnnotation(s)


// Example usage:
val shortStringType = AstSchema.SchemaContent.NamedType(
    name = "short_string",
    constraints = listOf(
        type(named("string")),
        codepointLength(atMost(50))
    )
)

val stateType = AstSchema.SchemaContent.NamedType(
    name = "State",
    constraints = listOf(
        validValues(
            """
                AK, AL, AR, AZ, CA, CO, CT, DE, FL, GA, HI, IA, ID, IL, IN, KS, KY,
                LA, MA, MD, ME, MI, MN, MO, MS, MT, NC, ND, NE, NH, NJ, NM, NV, NY,
                OH, OK, OR, PA, RI, SC, SD, TN, TX, UT, VA, VT, WA, WI, WV, WY
            """.split(",").map { ValidValue.SingleValue(ionSymbol(it.trim())) }
        )
    )
)

val addressType = AstSchema.SchemaContent.NamedType(
    name = "Address",
    constraints = listOf(
        type(named("struct")),
        fields(
            "address1" to inline(
                type(named(shortStringType.name)),
                occurs = exactly(1)
            ),
            "address2" to inline(
                type(named(shortStringType.name)),
                occurs = range(0, 1)
            ),
            "city" to inline(
                type(named("string")),
                codepointLength(range(0, 20)),
                occurs = exactly(1)
            ),
            "state" to inline(
                type(named(stateType.name)),
                occurs = exactly(1)
            ),
            "zipcode" to inline(
                type(named("int")),
                validValues(
                    ValidValue.NumberRange(BigDecimal.valueOf(10000), BigDecimal.valueOf(99999))
                ),
                occurs = exactly(1)
            ),
        )
    )
)

val customerType = AstSchema.SchemaContent.NamedType(
    name = "Customer",
    constraints = listOf(
        type(named("struct")),
        annotations("corporate", "gold_class", "club_member"),
        fields(
            "firstName" to named("string").occurs(exactly(1)),
            "middleName" to nullOr(named("\$string")).occurs(range(0, 1)),
            "lastName" to named("string").occurs(exactly(1)),
            "customerId" to inline(
                oneOf(
                    inline(
                        type(named("string")),
                        codepointLength(exactly(18))
                    ),
                    inline(
                        type(named("int")),
                        validValues(
                            ValidValue.NumberRange(BigDecimal.valueOf(100000), BigDecimal.valueOf(999999))
                        ),
                    )
                ),
                occurs = exactly(1)
            ),
            "addresses" to inline(
                type(named("list")),
                element(named(addressType.name)),
                containerLength(range(1, 7)),
                occurs = exactly(1)
            ),
            "lastUpdated" to inline(
                type(named("timestamp")),
                timestampPrecision(TimestampPrecisionRange(TimestampPrecisions.Second, TimestampPrecisions.Millisecond)),
                occurs = exactly(1)
            )
        )
    )
)
