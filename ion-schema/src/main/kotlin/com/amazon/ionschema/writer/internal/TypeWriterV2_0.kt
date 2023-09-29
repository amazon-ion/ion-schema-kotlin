// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal

import com.amazon.ion.IonWriter
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.DiscreteIntRange
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.NamedTypeDefinition
import com.amazon.ionschema.model.OpenContentFields
import com.amazon.ionschema.model.TypeArgument
import com.amazon.ionschema.model.TypeDefinition
import com.amazon.ionschema.model.VariablyOccurringTypeArgument
import com.amazon.ionschema.writer.internal.constraints.AnnotationsV2Writer
import com.amazon.ionschema.writer.internal.constraints.ContainsWriter
import com.amazon.ionschema.writer.internal.constraints.ElementWriter
import com.amazon.ionschema.writer.internal.constraints.ExponentWriter
import com.amazon.ionschema.writer.internal.constraints.FieldNamesWriter
import com.amazon.ionschema.writer.internal.constraints.FieldsWriter
import com.amazon.ionschema.writer.internal.constraints.Ieee754FloatWriter
import com.amazon.ionschema.writer.internal.constraints.LengthConstraintsWriter
import com.amazon.ionschema.writer.internal.constraints.LogicConstraintsWriter
import com.amazon.ionschema.writer.internal.constraints.OrderedElementsWriter
import com.amazon.ionschema.writer.internal.constraints.PrecisionWriter
import com.amazon.ionschema.writer.internal.constraints.RegexWriter
import com.amazon.ionschema.writer.internal.constraints.TimestampOffsetWriter
import com.amazon.ionschema.writer.internal.constraints.TimestampPrecisionWriter
import com.amazon.ionschema.writer.internal.constraints.ValidValuesWriter

@ExperimentalIonSchemaModel
internal object TypeWriterV2_0 : TypeWriter {
    private val constraintWriters = listOf(
        AnnotationsV2Writer(this),
        ContainsWriter,
        ElementWriter(this, IonSchemaVersion.v2_0),
        ExponentWriter,
        FieldNamesWriter(this),
        FieldsWriter(this, IonSchemaVersion.v2_0),
        Ieee754FloatWriter,
        LengthConstraintsWriter,
        LogicConstraintsWriter(this),
        OrderedElementsWriter(this),
        PrecisionWriter,
        RegexWriter,
        TimestampOffsetWriter,
        TimestampPrecisionWriter,
        ValidValuesWriter,
    ).flatMap { w -> w.supportedClasses.map { it to w } }
        .toMap()

    override fun writeNamedTypeDefinition(ionWriter: IonWriter, namedTypeDefinition: NamedTypeDefinition) {
        ionWriter.setTypeAnnotations("type")
        ionWriter.writeStruct {
            ionWriter.setFieldName("name")
            ionWriter.writeSymbol(namedTypeDefinition.typeName)
            writeConstraints(ionWriter, namedTypeDefinition.typeDefinition.constraints)
            writeOpenContent(ionWriter, namedTypeDefinition.typeDefinition.openContent)
        }
    }

    override fun writeTypeArg(ionWriter: IonWriter, typeArg: TypeArgument) {
        if (typeArg.nullability == TypeArgument.Nullability.OrNull) {
            ionWriter.addTypeAnnotation("\$null_or")
        }

        when (typeArg) {
            is TypeArgument.Import -> ionWriter.writeStruct {
                ionWriter.setFieldName("id")
                ionWriter.writeString(typeArg.schemaId)
                ionWriter.setFieldName("type")
                ionWriter.writeSymbol(typeArg.typeName)
            }
            is TypeArgument.InlineType -> ionWriter.writeStruct {
                writeConstraints(ionWriter, typeArg.typeDefinition.constraints)
                writeOpenContent(ionWriter, typeArg.typeDefinition.openContent)
            }
            is TypeArgument.Reference -> ionWriter.writeSymbol(typeArg.typeName)
        }
    }

    override fun writeVariablyOccurringTypeArg(ionWriter: IonWriter, varTypeArg: VariablyOccurringTypeArgument, elideOccursValue: DiscreteIntRange) {
        if (varTypeArg.occurs == elideOccursValue) {
            writeTypeArg(ionWriter, varTypeArg.typeArg)
        } else {
            ionWriter.writeStruct {
                setFieldName("occurs")
                writeRange(varTypeArg.occurs)
                if (varTypeArg.typeArg.nullability == TypeArgument.Nullability.None && varTypeArg.typeArg is TypeArgument.InlineType) {
                    writeConstraints(ionWriter, varTypeArg.typeArg.typeDefinition.constraints)
                    writeOpenContent(ionWriter, varTypeArg.typeArg.typeDefinition.openContent)
                } else {
                    setFieldName("type")
                    writeTypeArg(ionWriter, varTypeArg.typeArg)
                }
            }
        }
    }

    /**
     * Writes a type that exists outside the context of any schema.
     */
    fun writeOrphanedTypeDefinition(ionWriter: IonWriter, typeDefinition: TypeDefinition) {
        ionWriter.setTypeAnnotations("type")
        ionWriter.writeStruct {
            writeConstraints(ionWriter, typeDefinition.constraints)
            writeOpenContent(ionWriter, typeDefinition.openContent)
        }
    }

    private fun writeOpenContent(ionWriter: IonWriter, openContentFields: OpenContentFields) {
        for ((fieldName, fieldValue) in openContentFields) {
            ionWriter.setFieldName(fieldName)
            fieldValue.writeTo(ionWriter)
        }
    }

    private fun writeConstraints(ionWriter: IonWriter, constraints: Set<Constraint>) {
        for (c in constraints) {
            constraintWriters[c::class]?.writeTo(ionWriter, c)
                ?: TODO("Constraint not supported in Ion Schema 2.0: ${c::class}")
        }
    }
}
