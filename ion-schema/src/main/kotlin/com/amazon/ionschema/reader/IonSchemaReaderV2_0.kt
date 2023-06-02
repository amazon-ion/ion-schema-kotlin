package com.amazon.ionschema.reader

import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.internal.util.IonSchema_2_0
import com.amazon.ionschema.internal.util.getReadOnlyClone
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.NamedTypeDefinition
import com.amazon.ionschema.model.SchemaDocument
import com.amazon.ionschema.model.TypeDefinition
import com.amazon.ionschema.reader.internal.FooterReader
import com.amazon.ionschema.reader.internal.HeaderReader
import com.amazon.ionschema.reader.internal.ReadError
import com.amazon.ionschema.reader.internal.ReaderContext
import com.amazon.ionschema.reader.internal.TypeReaderV2_0
import com.amazon.ionschema.reader.internal.isFooter
import com.amazon.ionschema.reader.internal.isHeader
import com.amazon.ionschema.reader.internal.isTopLevelOpenContent
import com.amazon.ionschema.reader.internal.isType
import com.amazon.ionschema.reader.internal.readCatching
import com.amazon.ionschema.util.IonSchemaResult

@OptIn(ExperimentalIonSchemaModel::class)
class IonSchemaReaderV2_0 : IonSchemaReader {
    private val typeReader = TypeReaderV2_0()
    private val headerReader = HeaderReader(IonSchemaVersion.v2_0)
    private val footerReader = FooterReader { it in userReservedFields.footer || !IonSchema_2_0.RESERVED_WORDS_REGEX.matches(it) }

    private enum class ReaderState(val location: String) {
        Init("before version marker"),
        BeforeHeader("before schema header"),
        ReadingTypes("while reading types"),
    }

    override fun readSchema(document: Iterable<IonValue>, failFast: Boolean): IonSchemaResult<SchemaDocument, List<ReadError>> {
        val context = ReaderContext(failFast = failFast)

        val documentIterator = document.iterator()

        val schemaDocument = try { iterateSchema(documentIterator, context) } catch (e: InvalidSchemaException) { null }

        return if (schemaDocument != null && context.readErrors.isEmpty()) {
            IonSchemaResult.Ok(schemaDocument)
        } else {
            IonSchemaResult.Err(context.readErrors) { InvalidSchemaException("$it") }
        }
    }

    override fun readType(ion: IonValue, failFast: Boolean): IonSchemaResult<TypeDefinition, List<ReadError>> {
        val context = ReaderContext(failFast = failFast)
        val typeDefinition = try { typeReader.readOrphanedTypeDefinition(context, ion) } catch (e: InvalidSchemaException) { null }
        return if (typeDefinition != null && context.readErrors.isEmpty()) {
            IonSchemaResult.Ok(typeDefinition)
        } else {
            IonSchemaResult.Err(context.readErrors) { InvalidSchemaException("$it") }
        }
    }

    override fun readNamedType(ion: IonValue, failFast: Boolean): IonSchemaResult<NamedTypeDefinition, List<ReadError>> {
        val context = ReaderContext(failFast = failFast)
        val typeDefinition = try { typeReader.readNamedTypeDefinition(context, ion) } catch (e: InvalidSchemaException) { null }
        return if (typeDefinition != null && context.readErrors.isEmpty()) {
            IonSchemaResult.Ok(typeDefinition)
        } else {
            IonSchemaResult.Err(context.readErrors) { InvalidSchemaException("$it") }
        }
    }

    private fun iterateSchema(documentIterator: Iterator<IonValue>, context: ReaderContext): SchemaDocument? {
        val items = mutableListOf<SchemaDocument.Content>()
        var state = ReaderState.Init
        while (documentIterator.hasNext()) {
            val value = documentIterator.next()
            when {
                IonSchemaVersion.isVersionMarker(value) -> {
                    if (state == ReaderState.Init && IonSchemaVersion.fromIonSymbolOrNull(value) == IonSchemaVersion.v2_0) {
                        state = ReaderState.BeforeHeader
                    } else {
                        context.reportError(ReadError(value, "unexpected version marker ${state.location}"))
                    }
                }
                isHeader(value) -> {
                    if (state == ReaderState.BeforeHeader) {
                        readCatching(context, value) { headerReader.readHeader(context, value) }?.let(items::add)
                        state = ReaderState.ReadingTypes
                    } else {
                        context.reportError(ReadError(value, "schema header encountered ${state.location}"))
                    }
                }
                isType(value) -> {
                    if (state > ReaderState.Init) {
                        readCatching(context, value) { typeReader.readNamedTypeDefinition(context, value) }
                            ?.let { items.add(it) }
                        state = ReaderState.ReadingTypes
                    } else {
                        context.reportError(ReadError(value, "type definition encountered ${state.location}"))
                    }
                }
                isFooter(value) -> {
                    if (state > ReaderState.Init) {
                        readCatching(context, value) { items.add(footerReader.readFooter(context, value)) }
                        break
                    } else {
                        context.reportError(ReadError(value, "schema footer encountered ${state.location}"))
                    }
                }
                state > ReaderState.Init -> {
                    // If we've already seen the version marker, then handle open content
                    if (isTopLevelOpenContent(value)) {
                        items.add(SchemaDocument.OpenContent(value.getReadOnlyClone()))
                    } else {
                        context.reportError(ReadError(value, "invalid top level value ${state.location}"))
                    }
                }
                else -> {
                    // Drop anything before the version marker.
                }
            }
        }
        return SchemaDocument(null, IonSchemaVersion.v2_0, items.toList())
    }
}
