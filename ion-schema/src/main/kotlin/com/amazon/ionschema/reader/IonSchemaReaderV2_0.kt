package com.amazon.ionschema.reader

import com.amazon.ion.IonValue
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.NamedTypeDefinition
import com.amazon.ionschema.model.SchemaDocument
import com.amazon.ionschema.model.TypeDefinition
import com.amazon.ionschema.reader.internal.ReadError
import com.amazon.ionschema.reader.internal.ReaderContext
import com.amazon.ionschema.reader.internal.TypeReaderV2_0
import com.amazon.ionschema.util.IonSchemaResult

@OptIn(ExperimentalIonSchemaModel::class)
class IonSchemaReaderV2_0 : IonSchemaReader {
    private val typeReader = TypeReaderV2_0()

    override fun readSchema(document: List<IonValue>, failFast: Boolean): IonSchemaResult<SchemaDocument, List<ReadError>> {
        TODO()
    }

    override fun readType(ion: IonValue, failFast: Boolean): IonSchemaResult<TypeDefinition, List<ReadError>> {
        val context = ReaderContext(failFast = failFast)
        val typeDefinition = typeReader.readOrphanedTypeDefinition(context, ion)
        return if (context.readErrors.isEmpty()) {
            IonSchemaResult.Ok(typeDefinition)
        } else {
            IonSchemaResult.Err(context.readErrors)
        }
    }

    override fun readNamedType(ion: IonValue, failFast: Boolean): IonSchemaResult<NamedTypeDefinition, List<ReadError>> {
        TODO()
    }
}
