package com.amazon.ionschema.reader

import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.SchemaDocument
import com.amazon.ionschema.reader.internal.IonSchemaReaderV2_0
import com.amazon.ionschema.reader.internal.ReadError
import com.amazon.ionschema.reader.internal.VersionedIonSchemaReader
import com.amazon.ionschema.util.IonSchemaResult

/**
 * Reads IonValues into the Ion Schema model.
 */
@ExperimentalIonSchemaModel
object IonSchemaReader {
    /**
     * Attempts to read a [SchemaDocument]. Returns an [IonSchemaResult] containing either a [SchemaDocument] or a list
     * of [ReadError]s that were encountered.
     *
     * If [failFast] is true, returns once a single error has been detected. Otherwise, attempts to report all errors in
     * the given Ion. Reporting all errors is a best-effort attempt because the presence of one error can mask other
     * errors.
     */
    @JvmStatic
    @JvmOverloads
    fun readSchema(document: Iterable<IonValue>, failFast: Boolean = false): IonSchemaResult<SchemaDocument, List<ReadError>> {
        val maybeVersion = document
            .firstOrNull {
                (it is IonSymbol && !it.isNullValue && IonSchemaVersion.fromIonSymbolOrNull(it) != null) ||
                    it.hasTypeAnnotation("schema_header") ||
                    it.hasTypeAnnotation("schema_footer") ||
                    it.hasTypeAnnotation("type")
            }

        val version = maybeVersion
            ?.let { if (it is IonSymbol) IonSchemaVersion.fromIonSymbolOrNull(it) else null }
            ?: IonSchemaVersion.v1_0

        val delegate: VersionedIonSchemaReader = when (version) {
            IonSchemaVersion.v1_0 -> TODO("IonSchemaReader does not support Ion Schema 1.0 yet.")
            IonSchemaVersion.v2_0 -> IonSchemaReaderV2_0
        }
        return delegate.readSchema(document, failFast)
    }

    /**
     * Reads a [SchemaDocument], throwing an [InvalidSchemaException][com.amazon.ionschema.InvalidSchemaException]
     * if the Ion value is not a valid Ion Schema Language schema document.
     */
    fun readSchemaOrThrow(document: List<IonValue>) = readSchema(document, true).unwrap()
}
