// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal

import com.amazon.ion.IonWriter
import com.amazon.ionschema.model.DiscreteIntRange
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.NamedTypeDefinition
import com.amazon.ionschema.model.TypeArgument
import com.amazon.ionschema.model.TypeArguments
import com.amazon.ionschema.model.VariablyOccurringTypeArgument

@ExperimentalIonSchemaModel
internal interface TypeWriter {

    /**
     * Writes a [NamedTypeDefinition] to the given [IonWriter].
     */
    fun writeNamedTypeDefinition(ionWriter: IonWriter, namedTypeDefinition: NamedTypeDefinition)

    /**
     * Writes a [TypeArgument] to the given [IonWriter].
     */
    fun writeTypeArg(ionWriter: IonWriter, typeArg: TypeArgument)

    /**
     * Writes a [VariablyOccurringTypeArgument] to the given [IonWriter].
     */
    fun writeVariablyOccurringTypeArg(ionWriter: IonWriter, varTypeArg: VariablyOccurringTypeArgument, elideOccursValue: DiscreteIntRange)

    /**
     * Writes a [TypeArguments] to the given [IonWriter].
     */
    fun writeTypeArguments(ionWriter: IonWriter, typeArgs: TypeArguments) {
        ionWriter.writeList { typeArgs.forEach { writeTypeArg(ionWriter, it) } }
    }
}
