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
import com.amazon.ionschema.IonSchemaVersion
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
import kotlin.TODO

/**
 * Default [ConstraintFactory] implementation.
 *
 * Technically, it's a constraint factory factory, but we abstract that detail away.
 */
internal class ConstraintFactoryDefault : ConstraintFactory {

    // These are the actual constraint factories.
    private interface Constraints {
        val newInstance: (ion: IonValue, schema: Schema) -> Constraint
    }

    private enum class Constraints_1_0(override val newInstance: (ion: IonValue, schema: Schema) -> Constraint) : Constraints {
        all_of({ ion, schema -> AllOf(ion, schema) }),
        annotations({ ion, schema -> Annotations(ion) }),
        any_of({ ion, schema -> AnyOf(ion, schema) }),
        byte_length({ ion, schema -> ByteLength(ion) }),
        codepoint_length({ ion, schema -> CodepointLength(ion) }),
        container_length({ ion, schema -> ContainerLength(ion) }),
        contains({ ion, schema -> Contains(ion) }),
        content({ ion, schema -> Content(ion) }),
        element({ ion, schema -> Element(ion, schema) }),
        fields({ ion, schema -> Fields(ion, schema) }),
        not({ ion, schema -> Not(ion, schema) }),
        occurs({ ion, schema -> OccursNoop(ion) }),
        one_of({ ion, schema -> OneOf(ion, schema) }),
        ordered_elements({ ion, schema -> OrderedElements(ion, schema) }),
        precision({ ion, schema -> Precision(ion) }),
        regex({ ion, schema -> Regex(ion) }),
        scale({ ion, schema -> Scale(ion) }),
        timestamp_offset({ ion, schema -> TimestampOffset(ion) }),
        timestamp_precision({ ion, schema -> TimestampPrecision(ion) }),
        type({ ion, schema -> Type(ion, schema) }),
        utf8_byte_length({ ion, schema -> Utf8ByteLength(ion) }),
        valid_values({ ion, schema -> ValidValues(ion) }),
    }

    override fun isConstraint(name: String, schema: Schema) =
        try {
            when (schema.ionSchemaLanguageVersion) {
                IonSchemaVersion.ION_SCHEMA_1_0 -> Constraints_1_0.valueOf(name)
                else -> TODO("Ion Schema 2.0 support is not complete")
            }
            true
        } catch (e: IllegalArgumentException) {
            false
        }

    override fun constraintFor(ion: IonValue, schema: Schema) = when (schema.ionSchemaLanguageVersion) {
        IonSchemaVersion.ION_SCHEMA_1_0 -> Constraints_1_0.valueOf(ion.fieldName).newInstance(ion, schema)
        else -> TODO("Ion Schema 2.0 support is not complete")
    }
}
