package software.amazon.ionschema

import software.amazon.ion.IonValue

/**
 * Entry point for Ion Schema.  To create an [IonSchemaSystem], use [IonSchemaSystemBuilder].
 *
 * @see IonSchemaSystemBuilder
 */
interface IonSchemaSystem {
    /**
     * Requests the configured Authorities, in order, to resolve the requested
     * schema identifier until one successfully resolves it.
     *
     * @param[id] identifier for the schema to load
     * @throws IonSchemaException if the schema identifier cannot be resolved
     */
    fun loadSchema(id: String): Schema

    /**
     * Constructs a new, empty schema.
     *
     * @return the new schema
     */
    fun newSchema(): Schema

    /**
     * Constructs a new schema using ISL provided as a string.
     *
     * @param[isl] ISL string representation of the schema to create
     * @return the new schema
     */
    fun newSchema(isl: String): Schema

    /**
     * Constructs a new schema using ISL provided as Iterator<IonValue>.
     *
     * @param[isl] Iterator<IonValue> representing the desired schema
     * @return the new schema
     */
    fun newSchema(isl: Iterator<IonValue>): Schema
}

