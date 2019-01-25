package software.amazon.ionschema

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
}

