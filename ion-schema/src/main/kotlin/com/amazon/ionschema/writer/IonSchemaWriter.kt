// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer

import com.amazon.ion.IonWriter
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.NamedTypeDefinition
import com.amazon.ionschema.model.SchemaDocument
import com.amazon.ionschema.model.TypeDefinition

/**
 * Writes Ion Schema model to an IonWriter.
 */
@ExperimentalIonSchemaModel
interface IonSchemaWriter {
    /**
     * Writes a [SchemaDocument].
     */
    fun writeSchema(ionWriter: IonWriter, schemaDocument: SchemaDocument)

    /**
     * Writes an orphaned [TypeDefinition]—that is an anonymous type definition that does not belong to any schema.
     */
    fun writeType(ionWriter: IonWriter, typeDefinition: TypeDefinition)

    /**
     * Writes a [NamedTypeDefinition].
     */
    fun writeNamedType(ionWriter: IonWriter, namedTypeDefinition: NamedTypeDefinition)
}
