package com.amazon.ionschema.model

import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.internal.util.validateRegexPattern
import kotlin.text.Regex as KRegex

/**
 * Marker interface for all Constraint implementations.
 *
 * This implementation of Ion Schema does not support custom constraints. Do not implement this interface.
 */
@ExperimentalIonSchemaModel
interface Constraint {

    /**
     * Represents the `all_of` constraint.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#all_of) and
     * [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#all_of).
     */
    data class AllOf(val types: TypeArgumentList) : Constraint

    /**
     * Represents the `annotations` constraint for Ion Schema 1.0.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#annotations).
     */
    data class AnnotationsV1(val annotations: List<Annotation>, val closed: Boolean, val ordered: Boolean) :
        Constraint {
        class Annotation(val text: String, val required: Boolean)
    }

    /**
     * Represents the `annotations` constraint from Ion Schema 2.0 onwards.
     * See relevant section in [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#annotations).
     */
    data class AnnotationsV2(val type: TypeArgument) : Constraint {

        enum class Modifier { Closed, Required, ClosedAndRequired }
        companion object {
            /**
             * Factory method for [AnnotationsV2] that resembles the
             * [simple syntax](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#simple-syntax).
             */
            @JvmStatic
            fun create(modifier: Modifier, annotationSymbols: List<IonSymbol>): AnnotationsV2 {
                val annotationsConstraints = mutableSetOf<Constraint>()
                // If closed, constrain using `valid_values`
                if (modifier == Modifier.Closed || modifier == Modifier.ClosedAndRequired) {
                    val validValues = annotationSymbols.map { ValidValue.Value(it) }
                    annotationsConstraints.add(
                        Element(TypeArgument.InlineType(TypeDefinition(setOf(ValidValues(validValues)))))
                    )
                }
                // If required, constrain using `contains`
                if (modifier == Modifier.Required || modifier == Modifier.ClosedAndRequired) {
                    annotationsConstraints.add(Contains(annotationSymbols))
                }
                return AnnotationsV2(inlineType(annotationsConstraints))
            }
        }
    }

    /**
     * Represents the `any_of` constraint.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#any_of) and
     * [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#any_of).
     */
    data class AnyOf(val types: TypeArgumentList) : Constraint

    /**
     * Represents the `byte_length` constraint.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#byte_length) and
     * [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#byte_length).
     */
    data class ByteLength(val range: DiscreteIntRange) : Constraint {
        init {
            range.start?.let { require(it >= 0) }
        }
    }

    /**
     * Represents the `codepoint_length` constraint.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#codepoint_length) and
     * [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#codepoint_length).
     */
    data class CodepointLength(val range: DiscreteIntRange) : Constraint {
        init {
            range.start?.let { require(it >= 0) }
        }
    }

    /**
     * Represents the `container_length` constraint.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#container_length) and
     * [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#container_length).
     */
    data class ContainerLength(val range: DiscreteIntRange) : Constraint {
        init {
            range.start?.let { require(it >= 0) }
        }
    }

    /**
     * Represents the `contains` constraint.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#contains) and
     * [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#contains).
     */
    data class Contains(val values: List<IonValue>) : Constraint

    /**
     * Represents the `element` constraint.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#element) and
     * [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#element).
     *
     * The [distinct] modifier is only supported for Ion Schema 2.0 and higher.
     */
    data class Element(val type: TypeArgument, val distinct: Boolean = false) : Constraint

    /**
     * Represents the `exponent` constraint, introduced in Ion Schema 2.0.
     * See relevant section in [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#exponent).
     */
    data class Exponent(val range: DiscreteIntRange) : Constraint

    /**
     * Represents the `field_names` constraint, introduced in Ion Schema 2.0.
     * See relevant section in [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#field_names).
     */
    data class FieldNames(val type: TypeArgument, val distinct: Boolean = false) : Constraint

    /**
     * Represents the `fields` constraint.
     * Beware when reading from ISL—the ISL representation of [closed] was changed between ISL 1.0 and ISL 2.0.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#fields) and
     * [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#fields).
     */
    data class Fields(val fields: Map<String, VariablyOccurringTypeArgument>, val closed: Boolean) : Constraint

    /**
     * Represents the `ieee754_float` constraint, introduced in Ion Schema 2.0.
     * See relevant section in [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#ieee754_float).
     */
    data class Ieee754Float(val format: Ieee754InterchangeFormat) : Constraint

    /**
     * Represents the `not` constraint.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#not) and
     * [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#not).
     */
    data class Not(val type: TypeArgument) : Constraint

    /**
     * Represents the `one_of` constraint.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#one_of) and
     * [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#one_of).
     */
    data class OneOf(val types: TypeArgumentList) : Constraint

    /**
     * Represents the `ordered_elements` constraint.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#ordered_elements) and
     * [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#ordered_elements).
     */
    data class OrderedElements(val types: List<VariablyOccurringTypeArgument>) : Constraint

    /**
     * Represents the `precision` constraint.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#precision) and
     * [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#precision).
     */
    data class Precision(val range: DiscreteIntRange) : Constraint {
        init {
            range.start?.let { require(it >= 1) }
        }
    }

    /**
     * Represents the `regex` constraint.
     *
     * Ion Schema regular expressions are a subset of ECMA-262.
     * The allowed subset of ECMA-262 regular expressions was changed between ISL 1.0 and ISL 2.0.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#regex) and
     * [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#regex).
     *
     * See also [_Comparison of regular expression engines_](https://en.wikipedia.org/wiki/Comparison_of_regular_expression_engines#Language_features)
     * for the high-level differences between ECMA (JavaScript) and Java regular expressions.
     */
    data class Regex(
        val pattern: String,
        val caseInsensitive: Boolean = false,
        val multiline: Boolean = false,
        val ionSchemaVersion: IonSchemaVersion = IonSchemaVersion.v2_0,
    ) : Constraint {
        internal val compiled = run {
            require(pattern.isNotEmpty())
            val opts = mutableSetOf<RegexOption>()
            if (caseInsensitive) opts.add(RegexOption.IGNORE_CASE)
            if (multiline) opts.add(RegexOption.MULTILINE)
            KRegex(validateRegexPattern(pattern, ionSchemaVersion), opts)
        }
    }

    /**
     * Represents the `scale` constraint.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#scale).
     *
     * This constraint was replaced by `exponent` in Ion Schema 2.0.
     * To convert to [Exponent], used [Scale.toExponentConstraint].
     */
    data class Scale(val range: DiscreteIntRange) : Constraint {
        fun toExponentConstraint() {
            Exponent(range.negate())
        }
    }

    /**
     * Represents the `timestamp_offset` constraint.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#timestamp_offset) and
     * [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#timestamp_offset).
     *
     * @see TimestampOffsetValue
     */
    data class TimestampOffset(val offsets: List<TimestampOffsetValue>) : Constraint

    /**
     * Represents the `timestamp_precision` constraint.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#timestamp_precision) and
     * [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#timestamp_precision).
     *
     * @see [TimestampPrecisionRange]
     */
    data class TimestampPrecision(val range: TimestampPrecisionRange) : Constraint

    /**
     * Represents the `type` constraint.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#type) and
     * [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#type).
     */
    data class Type(val type: TypeArgument) : Constraint

    /**
     * Represents the `utf8_byte_length` constraint.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#utf8_byte_length) and
     * [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#utf8_byte_length).
     */
    data class Utf8ByteLength(val range: DiscreteIntRange) : Constraint {
        init {
            range.start?.let { require(it >= 0) }
        }
    }

    /**
     * Represents the `valid_values` constraint.
     * See relevant section in [ISL 1.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#valid_values) and
     * [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#valid_values).
     *
     * @see ValidValue
     */
    data class ValidValues(val values: List<ValidValue>) : Constraint
}
