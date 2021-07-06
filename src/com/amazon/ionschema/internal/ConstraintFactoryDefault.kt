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

import com.amazon.ion.IonValue
import com.amazon.ionschema.Schema
import com.amazon.ionschema.internal.constraint.AllOf
import com.amazon.ionschema.internal.constraint.Annotations
import com.amazon.ionschema.internal.constraint.AnyOf
import com.amazon.ionschema.internal.constraint.ByteLength
import com.amazon.ionschema.internal.constraint.CodepointLength
import com.amazon.ionschema.internal.constraint.ContainerLength
import com.amazon.ionschema.internal.constraint.Contains
import com.amazon.ionschema.internal.constraint.Content
import com.amazon.ionschema.internal.constraint.Element
import com.amazon.ionschema.internal.constraint.Fields
import com.amazon.ionschema.internal.constraint.FloatSize
import com.amazon.ionschema.internal.constraint.Not
import com.amazon.ionschema.internal.constraint.OccursNoop
import com.amazon.ionschema.internal.constraint.OneOf
import com.amazon.ionschema.internal.constraint.OrderedElements
import com.amazon.ionschema.internal.constraint.Precision
import com.amazon.ionschema.internal.constraint.Regex
import com.amazon.ionschema.internal.constraint.Scale
import com.amazon.ionschema.internal.constraint.TimestampOffset
import com.amazon.ionschema.internal.constraint.TimestampPrecision
import com.amazon.ionschema.internal.constraint.Type
import com.amazon.ionschema.internal.constraint.Utf8ByteLength
import com.amazon.ionschema.internal.constraint.ValidValues

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
        float_size,
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
        utf8_byte_length,
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
            Constraints.all_of -> AllOf(ion, schema)
            Constraints.annotations -> Annotations(ion)
            Constraints.any_of -> AnyOf(ion, schema)
            Constraints.byte_length -> ByteLength(ion)
            Constraints.codepoint_length -> CodepointLength(ion)
            Constraints.container_length -> ContainerLength(ion)
            Constraints.contains -> Contains(ion)
            Constraints.content -> Content(ion)
            Constraints.element -> Element(ion, schema)
            Constraints.fields -> Fields(ion, schema)
            Constraints.float_size -> FloatSize(ion)
            Constraints.not -> Not(ion, schema)
            Constraints.occurs -> OccursNoop(ion)
            Constraints.one_of -> OneOf(ion, schema)
            Constraints.ordered_elements -> OrderedElements(ion, schema)
            Constraints.precision -> Precision(ion)
            Constraints.regex -> Regex(ion)
            Constraints.scale -> Scale(ion)
            Constraints.timestamp_offset -> TimestampOffset(ion)
            Constraints.timestamp_precision -> TimestampPrecision(ion)
            Constraints.type -> Type(ion, schema)
            Constraints.utf8_byte_length -> Utf8ByteLength(ion)
            Constraints.valid_values -> ValidValues(ion)
        }
}
