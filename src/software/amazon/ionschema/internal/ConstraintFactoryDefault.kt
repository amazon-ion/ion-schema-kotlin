package software.amazon.ionschema.internal

import software.amazon.ion.IonValue
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.constraint.*

/**
 * Default [ConstraintFactory] implementation.
 */
internal class ConstraintFactoryDefault : ConstraintFactory {
    private enum class Constraints {
        all_of,
        annotations,
        any_of,
        byte_length,
        codepoint_length,
        container_length,
        contains,
        content,
        element,
        fields,
        not,
        occurs,
        one_of,
        ordered_elements,
        precision,
        regex,
        scale,
        timestamp_offset,
        timestamp_precision,
        type,
        valid_values,
    }

    override fun isConstraint(name: String) =
        try {
            Constraints.valueOf(name)
            true
        } catch (e: IllegalArgumentException) {
            false
        }

    override fun constraintFor(ion: IonValue, schema: Schema) =
        when (Constraints.valueOf(ion.fieldName)) {
            Constraints.all_of              -> AllOf(ion, schema)
            Constraints.annotations         -> Annotations(ion)
            Constraints.any_of              -> AnyOf(ion, schema)
            Constraints.byte_length         -> ByteLength(ion)
            Constraints.codepoint_length    -> CodepointLength(ion)
            Constraints.container_length    -> ContainerLength(ion)
            Constraints.contains            -> Contains(ion)
            Constraints.content             -> Content(ion)
            Constraints.element             -> Element(ion, schema)
            Constraints.fields              -> Fields(ion, schema)
            Constraints.not                 -> Not(ion, schema)
            Constraints.occurs              -> OccursNoop(ion)
            Constraints.one_of              -> OneOf(ion, schema)
            Constraints.ordered_elements    -> OrderedElements(ion, schema)
            Constraints.precision           -> Precision(ion)
            Constraints.regex               -> Regex(ion)
            Constraints.scale               -> Scale(ion)
            Constraints.timestamp_offset    -> TimestampOffset(ion)
            Constraints.timestamp_precision -> TimestampPrecision(ion)
            Constraints.type                -> Type(ion, schema)
            Constraints.valid_values        -> ValidValues(ion)
        }
}

