package com.amazon.ionschema.reader.internal

import com.amazon.ion.IonList
import com.amazon.ion.IonValue
import com.amazon.ionschema.internal.util.islRequire
import com.amazon.ionschema.internal.util.islRequireIonTypeNotNull
import com.amazon.ionschema.model.DiscreteIntRange
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.NamedTypeDefinition
import com.amazon.ionschema.model.TypeArgument
import com.amazon.ionschema.model.TypeArguments
import com.amazon.ionschema.model.VariablyOccurringTypeArgument

@ExperimentalIonSchemaModel
internal interface TypeReader {

    /**
     * Reads a [NamedTypeDefinition].
     */
    fun readNamedTypeDefinition(context: ReaderContext, ion: IonValue): NamedTypeDefinition

    /**
     * Reads a [TypeArgument].
     */
    fun readTypeArg(context: ReaderContext, ion: IonValue, checkAnnotations: Boolean = true): TypeArgument

    /**
     * Reads a [VariablyOccurringTypeArgument].
     */
    fun readVariablyOccurringTypeArg(context: ReaderContext, ion: IonValue, defaultOccurs: DiscreteIntRange): VariablyOccurringTypeArgument

    /**
     * Reads a [TypeArguments].
     */
    fun readTypeArgumentList(context: ReaderContext, ion: IonValue): TypeArguments {
        val constraintName = ion.fieldName!!
        islRequireIonTypeNotNull<IonList>(ion) { "Illegal argument for '$constraintName' constraint; must be non-null Ion list: $ion" }
        islRequire(ion.typeAnnotations.isEmpty()) { "Illegal argument for '$constraintName' constraint; must not have annotations; was: $ion" }
        return ion.readAllCatching(context) { readTypeArg(context, it) }.toSet()
    }
}
