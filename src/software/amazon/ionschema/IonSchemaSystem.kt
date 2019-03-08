package software.amazon.ionschema

import software.amazon.ion.IonValue

/**
 * Provides methods for instantiating instances of [Schema].
 *
 * To create an instance, use [IonSchemaSystemBuilder].
 */
interface IonSchemaSystem {
    /**
     * Requests each of the provided [Authority]s, in order, to resolve
     * the requested schema id until one successfully resolves it.
     *
     * If an Authority throws an exception, resolution silently proceeds
     * to the next Authority.
     *
     * @param[id] Identifier for the schema to load.
     * @throws IonSchemaException if the schema id cannot be resolved.
     * @throws InvalidSchemaException if the schema, once resolved, is determined to be invalid.
     */
    fun loadSchema(id: String): Schema

    /**
     * Constructs a new, empty schema.
     *
     * @return the new schema
     */
    fun newSchema(): Schema

    /**
     * Constructs a new schema using ISL provided as a String.
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

