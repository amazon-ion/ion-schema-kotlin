package com.amazon.ionschema.reader

import com.amazon.ion.IonValue
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.NamedTypeDefinition
import com.amazon.ionschema.model.SchemaDocument
import com.amazon.ionschema.model.TypeDefinition
import com.amazon.ionschema.reader.internal.ReadError
import com.amazon.ionschema.util.IonSchemaResult

/**
 * Reads IonValues into the Ion Schema model.
 */
@ExperimentalIonSchemaModel
interface IonSchemaReader {
    /**
     * Attempts to read a [SchemaDocument]. Returns an [IonSchemaResult] containing either a [SchemaDocument] or a list
     * of [ReadError]s that were encountered.
     *
     * If [failFast] is true, returns once a single error has been detected. Otherwise, attempts to report all errors in
     * the given Ion. Reporting all errors is a best-effort attempt because the presence of one error can mask other
     * errors.
     */
    fun readSchema(document: Iterable<IonValue>, failFast: Boolean = false): IonSchemaResult<Pair<SchemaDocument, Iterator<IonValue>>, List<ReadError>>

    /**
     * Reads a [SchemaDocument], throwing an [InvalidSchemaException][com.amazon.ionschema.InvalidSchemaException]
     * if the Ion value is not a valid Ion Schema Language schema document.
     */
    fun readSchemaOrThrow(document: List<IonValue>) = readSchema(document, true).unwrap()

    /**
     * Reads an orphaned [TypeDefinition]—that is an anonymous type definition that does not belong to any schema.
     * Returns an [IonSchemaResult] containing either a [TypeDefinition] or a list of [ReadError]s that were encountered.
     *
     * If [failFast] is true, returns once a single error has been detected. Otherwise, attempts to report all errors in
     * the given Ion. Reporting all errors is a best-effort attempt because the presence of one error can mask other
     * errors.
     */
    fun readType(ion: IonValue, failFast: Boolean = false): IonSchemaResult<TypeDefinition, List<ReadError>>

    /**
     * Reads an orphaned [TypeDefinition]—that is an anonymous type definition that does not belong to any schema—
     * throwing an [InvalidSchemaException][com.amazon.ionschema.InvalidSchemaException] if the Ion value is not a valid
     * Ion Schema type definition.
     */
    fun readTypeOrThrow(ion: IonValue) = readType(ion, true).unwrap()

    /**
     * Reads a [NamedTypeDefinition]. Returns an [IonSchemaResult] containing either a [NamedTypeDefinition] or a list
     * of [ReadError]s that were encountered.
     *
     * If [failFast] is true, returns once a single error has been detected. Otherwise, attempts to report all errors in
     * the given Ion. Reporting all errors is a best-effort attempt because the presence of one error can mask other
     * errors.
     */
    fun readNamedType(ion: IonValue, failFast: Boolean = false): IonSchemaResult<NamedTypeDefinition, List<ReadError>>

    /**
     * Reads a [NamedTypeDefinition], throwing an [InvalidSchemaException][com.amazon.ionschema.InvalidSchemaException]
     * if the Ion value is not a valid Ion Schema named type definition.
     */
    fun readNamedTypeOrThrow(ion: IonValue) = readNamedType(ion, true).unwrap()
}
