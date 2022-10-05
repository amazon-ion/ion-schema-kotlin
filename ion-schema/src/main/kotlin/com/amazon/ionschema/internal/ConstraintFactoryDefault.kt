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
import com.amazon.ionschema.IonSchemaVersion.v1_0
import com.amazon.ionschema.IonSchemaVersion.v2_0
import com.amazon.ionschema.Schema
import com.amazon.ionschema.internal.constraint.AllOf
import com.amazon.ionschema.internal.constraint.Annotations_1_0
import com.amazon.ionschema.internal.constraint.AnyOf
import com.amazon.ionschema.internal.constraint.ByteLength
import com.amazon.ionschema.internal.constraint.CodepointLength
import com.amazon.ionschema.internal.constraint.ContainerLength
import com.amazon.ionschema.internal.constraint.Contains
import com.amazon.ionschema.internal.constraint.Content
import com.amazon.ionschema.internal.constraint.Element
import com.amazon.ionschema.internal.constraint.Exponent
import com.amazon.ionschema.internal.constraint.FieldNames
import com.amazon.ionschema.internal.constraint.Fields
import com.amazon.ionschema.internal.constraint.Ieee754Float
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

    /**
     * Represents a mapping of (constraint name + supported ISL versions) to a constructor function
     */
    private data class ConstraintConstructor(
        val name: String,
        val versions: ClosedRange<IonSchemaVersion>,
        val newInstance: (ion: IonValue, schema: Schema) -> Constraint,
    ) {
        constructor(name: String, versions: ClosedRange<IonSchemaVersion>, newInstance: (IonValue) -> Constraint) : this(name, versions, { ion, _ -> newInstance(ion) })
        constructor(name: String, version: IonSchemaVersion, newInstance: (IonValue) -> Constraint) : this(name, version..version, { ion, _ -> newInstance(ion) })
        constructor(name: String, version: IonSchemaVersion, newInstance: (IonValue, schema: Schema) -> Constraint) : this(name, version..version, newInstance)
    }

    private val constraints = listOf(
        ConstraintConstructor("all_of", v1_0..v2_0, ::AllOf),
        ConstraintConstructor("annotations", v1_0, ::Annotations_1_0),
        ConstraintConstructor("any_of", v1_0..v2_0, ::AnyOf),
        ConstraintConstructor("byte_length", v1_0..v2_0, ::ByteLength),
        ConstraintConstructor("codepoint_length", v1_0..v2_0, ::CodepointLength),
        ConstraintConstructor("container_length", v1_0..v2_0, ::ContainerLength),
        ConstraintConstructor("contains", v1_0..v2_0, ::Contains),
        ConstraintConstructor("content", v1_0, ::Content),
        ConstraintConstructor("element", v1_0..v2_0, ::Element),
        ConstraintConstructor("exponent", v2_0, ::Exponent),
        ConstraintConstructor("field_names", v2_0, ::FieldNames),
        ConstraintConstructor("fields", v1_0..v2_0, ::Fields),
        ConstraintConstructor("ieee754_float", v2_0, ::Ieee754Float),
        ConstraintConstructor("not", v1_0..v2_0, ::Not),
        ConstraintConstructor("occurs", v1_0, ::OccursNoop),
        ConstraintConstructor("one_of", v1_0..v2_0, ::OneOf),
        ConstraintConstructor("ordered_elements", v1_0..v2_0, ::OrderedElements),
        ConstraintConstructor("precision", v1_0..v2_0, ::Precision),
        ConstraintConstructor("regex", v1_0..v2_0) { ion, schema -> Regex(ion, schema.ionSchemaLanguageVersion) },
        ConstraintConstructor("scale", v1_0, ::Scale),
        ConstraintConstructor("timestamp_offset", v1_0..v2_0, ::TimestampOffset),
        ConstraintConstructor("timestamp_precision", v1_0..v2_0, ::TimestampPrecision),
        ConstraintConstructor("type", v1_0..v2_0, ::Type),
        ConstraintConstructor("utf8_byte_length", v1_0..v2_0, ::Utf8ByteLength),
        ConstraintConstructor("valid_values", v1_0..v2_0, ::ValidValues),
    )

    override fun isConstraint(name: String, version: IonSchemaVersion): Boolean {
        return constraints.any { name == it.name && version in it.versions }
    }

    override fun constraintFor(ion: IonValue, schema: Schema) = constraints
        .single { ion.fieldName == it.name && schema.ionSchemaLanguageVersion in it.versions }
        .newInstance(ion, schema)
}
