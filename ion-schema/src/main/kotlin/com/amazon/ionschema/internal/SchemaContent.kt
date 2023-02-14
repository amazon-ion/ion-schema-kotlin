package com.amazon.ionschema.internal

import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.internal.util.getIslRequiredField

/**
 * A wrapper class for a stream of IonValue that is expected to be an Ion Schema document.
 *
 * The purpose of this class is similar to a C header file—so that we can know about all the declared types in a schema
 * document before we've fully loaded the schema document into the schema system. This is essential for being able to
 * resolve cyclical dependencies. See also [Forward Declaration](https://en.wikipedia.org/wiki/Forward_declaration).
 *
 * This class performs some processing in its initialization block in order to determine the Ion Schema version and
 * the names of all the types declared by the schema document, but it does not perform any syntactic or semantic
 * validation of the schema document.
 */
internal class SchemaContent(val isl: List<IonValue>) {
    val version: IonSchemaVersion
    val declaredTypes: List<IonSymbol>
    init {
        var version: IonSchemaVersion = IonSchemaVersion.v1_0
        declaredTypes = isl
            .onEach {
                if (it is IonSymbol && IonSchemaVersion.VERSION_MARKER_REGEX.matches(it.stringValue())) {
                    version = IonSchemaVersion.fromIonSymbolOrNull(it)
                        ?: throw InvalidSchemaException("Unsupported Ion Schema version: $it")
                }
            }
            .filterIsInstance<IonStruct>()
            .filter { it.hasTypeAnnotation("type") }
            .map { it.getIslRequiredField("name") }
        this.version = version
    }
}
