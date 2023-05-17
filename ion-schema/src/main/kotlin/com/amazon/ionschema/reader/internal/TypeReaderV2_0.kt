package com.amazon.ionschema.reader.internal

import com.amazon.ion.IonStruct
import com.amazon.ion.IonValue
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.internal.util.islRequire
import com.amazon.ionschema.internal.util.islRequireIonTypeNotNull
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.DiscreteIntRange
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.NamedTypeDefinition
import com.amazon.ionschema.model.TypeArgument
import com.amazon.ionschema.model.TypeDefinition
import com.amazon.ionschema.model.VariablyOccurringTypeArgument
import com.amazon.ionschema.reader.internal.constraints.Ieee754FloatReader
import com.amazon.ionschema.reader.internal.constraints.RegexReader
import com.amazon.ionschema.util.toBag

@ExperimentalIonSchemaModel
internal class TypeReaderV2_0 : TypeReader {

    private val constraintReaders = listOf(
        Ieee754FloatReader(),
        RegexReader(IonSchemaVersion.v2_0),
    )

    override fun readNamedTypeDefinition(context: ReaderContext, ion: IonValue): NamedTypeDefinition {
        TODO()
    }

    override fun readTypeArg(context: ReaderContext, ion: IonValue, checkAnnotations: Boolean): TypeArgument {
        TODO()
    }

    override fun readVariablyOccurringTypeArg(context: ReaderContext, ion: IonValue, defaultOccurs: DiscreteIntRange): VariablyOccurringTypeArgument {
        TODO()
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
