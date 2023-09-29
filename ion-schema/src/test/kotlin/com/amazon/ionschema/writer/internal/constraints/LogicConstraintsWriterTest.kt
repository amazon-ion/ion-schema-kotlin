// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.TypeArgument.Reference

@OptIn(ExperimentalIonSchemaModel::class)
class LogicConstraintsWriterTest : ConstraintTestBase(
    writer = LogicConstraintsWriter(stubTypeWriterWithRefs("foo", "bar")),
    expectedConstraints = setOf(
        Constraint.AllOf::class,
        Constraint.AnyOf::class,
        Constraint.OneOf::class,
        Constraint.Not::class,
        Constraint.Type::class,
    ),
    writeTestCases = listOf(
        Constraint.AllOf(emptySet()) to "all_of: []",
        Constraint.AllOf(Reference("foo")) to "all_of: [foo]",
        Constraint.AllOf(Reference("foo"), Reference("bar")) to "all_of: [foo, bar]",
        Constraint.AnyOf(emptySet()) to "any_of: []",
        Constraint.AnyOf(Reference("foo")) to "any_of: [foo]",
        Constraint.AnyOf(Reference("foo"), Reference("bar")) to "any_of: [foo, bar]",
        Constraint.OneOf(emptySet()) to "one_of: []",
        Constraint.OneOf(Reference("foo")) to "one_of: [foo]",
        Constraint.OneOf(Reference("foo"), Reference("bar")) to "one_of: [foo, bar]",
        Constraint.Not(Reference("foo")) to "not: foo",
        Constraint.Type(Reference("foo")) to "type: foo",
    )
)
