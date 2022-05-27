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
package com.amazon.ionschema.model

import com.amazon.ionschema.model.constraints.*

/**
 * An enumeration of all known (ie. built-in) [ConstraintId]s.
 */
enum class KnownConstraintIds(id: AnyConstraintId) : AnyConstraintId by id {
    AllOf(AllOfConstraint),
    Annotations(AnnotationsConstraint),
    AnyOf(AnyOfConstraint),
    ByteLength(ByteLengthConstraint),
    CodepointLength(CodepointLengthConstraint),
    ContainerLength(ContainerLengthConstraint),
    Contains(ContainsConstraint),
    Element(ElementConstraint),
    Fields(FieldsConstraint),
    Not(NotConstraint),
    OneOf(OneOfConstraint),
    OrderedElements(OrderedElementsConstraint),
    Precision(PrecisionConstraint),
    Regex(RegexConstraint),
    Scale(ScaleConstraint),
    TimestampOffset(TimestampOffsetConstraint),
    TimestampPrecision(TimestampPrecisionConstraint),
    Type(TypeConstraint),
    Utf8ByteLength(Utf8ByteLengthConstraint),
    ValidValues(ValidValuesConstraint);
}
