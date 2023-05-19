package com.amazon.ionschema.reader.internal

import com.amazon.ion.IonInt
import com.amazon.ion.IonList
import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonText
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.internal.util.getIslOptionalField
import com.amazon.ionschema.internal.util.getIslRequiredField
import com.amazon.ionschema.internal.util.islRequire
import com.amazon.ionschema.internal.util.islRequireExactAnnotations
import com.amazon.ionschema.internal.util.islRequireIonTypeNotNull
import com.amazon.ionschema.internal.util.islRequireNoIllegalAnnotations
import com.amazon.ionschema.internal.util.islRequireOnlyExpectedFieldNames
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.DiscreteIntRange
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.NamedTypeDefinition
import com.amazon.ionschema.model.TypeArgument
import com.amazon.ionschema.model.TypeDefinition
import com.amazon.ionschema.model.VariablyOccurringTypeArgument
import com.amazon.ionschema.reader.internal.constraints.ElementV2Reader
import com.amazon.ionschema.reader.internal.constraints.ExponentReader
import com.amazon.ionschema.reader.internal.constraints.FieldNamesReader
import com.amazon.ionschema.reader.internal.constraints.FieldsV2Reader
import com.amazon.ionschema.reader.internal.constraints.Ieee754FloatReader
import com.amazon.ionschema.reader.internal.constraints.LengthConstraintsReader
import com.amazon.ionschema.reader.internal.constraints.LogicConstraintsReader
import com.amazon.ionschema.reader.internal.constraints.OrderedElementsReader
import com.amazon.ionschema.reader.internal.constraints.PrecisionReader
import com.amazon.ionschema.reader.internal.constraints.RegexReader
import com.amazon.ionschema.util.toBag

@ExperimentalIonSchemaModel
internal class TypeReaderV2_0 : TypeReader {

    private val constraintReaders = listOf(
        ElementV2Reader(this),
        ExponentReader(),
        FieldNamesReader(this),
        FieldsV2Reader(this),
        Ieee754FloatReader(),
        LengthConstraintsReader(),
        LogicConstraintsReader(this),
        OrderedElementsReader(this),
        PrecisionReader(),
        RegexReader(IonSchemaVersion.v2_0),
    )

    override fun readNamedTypeDefinition(context: ReaderContext, ion: IonValue): NamedTypeDefinition {
        islRequireIonTypeNotNull<IonStruct>(ion) { "Named type definitions must be a non-null struct; was: $ion" }
        islRequireExactAnnotations(ion, "type") { "Named type definitions must be annotated with 'type' and nothing else: $ion" }
        islRequire(!ion.containsKey("occurs")) { "Named type definitions may not have an 'occurs' field: $ion" }
        return NamedTypeDefinition(
            ion.getIslRequiredField<IonSymbol>("name").stringValue(),
            privateReadTypeDefinition(context, ion)
        )
    }

    override fun readTypeArg(context: ReaderContext, ion: IonValue, checkAnnotations: Boolean): TypeArgument {
        if (checkAnnotations) islRequire(ion.typeAnnotations.isEmpty() || ion.typeAnnotations.single() == "\$null_or") {
            "Invalid constraint; illegal annotation on type argument: $ion"
        }
        val nullability = if (ion.hasTypeAnnotation("\$null_or")) TypeArgument.Nullability.OrNull else TypeArgument.Nullability.None
        return when {
            ion is IonStruct && ion.containsKey("id") -> {
                ion.islRequireOnlyExpectedFieldNames(listOf("id", "type"))
                TypeArgument.Import(
                    schemaId = ion.getIslRequiredField<IonText>("id").stringValue(),
                    typeName = ion.getIslRequiredField<IonSymbol>("type").stringValue(),
                    nullability = nullability,
                ).also { context.unresolvedReferences.add(it) }
            }
            ion is IonStruct -> {
                islRequire(!ion.containsKey("name")) { "Inline type definitions may not have a 'name' field" }
                islRequire(!ion.containsKey("occurs")) { "Inline type definitions may not have a 'occurs' field" }

                TypeArgument.InlineType(
                    typeDefinition = privateReadTypeDefinition(context, ion),
                    nullability = nullability,
                )
            }
            ion is IonSymbol -> TypeArgument.Reference(ion.stringValue(), nullability).also { context.unresolvedReferences.add(it) }
            else -> throw InvalidSchemaException("Invalid constraint; not a valid type argument: $ion")
        }
    }

    override fun readVariablyOccurringTypeArg(context: ReaderContext, ion: IonValue, defaultOccurs: DiscreteIntRange): VariablyOccurringTypeArgument {
        return if (ion is IonStruct && ion.containsKey("occurs")) {
            islRequireNoIllegalAnnotations(ion) { "Variably occurring type argument may not be annotated" }

            val occursField = ion.getIslOptionalField<IonValue>("occurs", allowAnnotations = true)!!
            if (occursField !is IonList) islRequireNoIllegalAnnotations(occursField) { "Illegal annotation on 'occurs' argument: $occursField" }
            val occurs = when (occursField) {
                is IonSymbol -> when (occursField.stringValue()) {
                    "optional" -> VariablyOccurringTypeArgument.OCCURS_OPTIONAL
                    "required" -> VariablyOccurringTypeArgument.OCCURS_REQUIRED
                    else -> null
                }
                is IonInt -> occursField.intValue().takeIf { it > 0 }?.let { DiscreteIntRange(it, it) }
                is IonList -> occursField.toDiscreteIntRange().takeIf { (it.start ?: 0) >= 0 }
                else -> null
            } ?: throw InvalidSchemaException("Invalid 'occurs' value; must be 'optional', 'required', a positive int, or a non-negative int range: $occursField")

            islRequire(!ion.containsKey("name")) { "Variably occurring type argument may not have a 'name' field" }
            VariablyOccurringTypeArgument(occurs, TypeArgument.InlineType(privateReadTypeDefinition(context, ion)))
        } else {
            VariablyOccurringTypeArgument(defaultOccurs, readTypeArg(context, ion))
        }
    }

    /**
     * Reads a type that exists outside the context of any schema.
     */
    fun readOrphanedTypeDefinition(context: ReaderContext, ion: IonValue): TypeDefinition {
        islRequireIonTypeNotNull<IonStruct>(ion) { "Type definitions must be a non-null struct; was: $ion" }
        islRequire(!ion.containsKey("name")) { "Anonymous type definitions may not have a 'name' field" }
        islRequire(!ion.containsKey("occurs")) { "Anonymous type definitions may not have a 'occurs' field" }
        return privateReadTypeDefinition(context, ion)
    }

    /**
     * Common functionality for reading named type definitions, inline types, and variably occurring type args.
     * The fields "name" and "occurs" must be handled by the calling function.
     */
    private fun privateReadTypeDefinition(context: ReaderContext, type: IonStruct): TypeDefinition {
        val constraints = mutableListOf<Constraint>()
        val openContent = mutableListOf<Pair<String, IonValue>>()

        type.readAllCatching(context) { field ->
            val fieldName = field.fieldName
            if (fieldName == "occurs" || fieldName == "name") {
                // Skip 'occurs' and 'name' -- they are handled elsewhere
            } else {
                val readerForThisConstraint = constraintReaders.firstOrNull { it.canRead(fieldName) }

                if (readerForThisConstraint != null) {
                    constraints.add(readerForThisConstraint.readConstraint(context, field))
                } else {
                    // TODO: Make sure that it's a legal field name for open content
                    openContent.add(fieldName to field)
                }
            }
        }
        return TypeDefinition(constraints.toSet(), openContent.toBag())
    }
}
