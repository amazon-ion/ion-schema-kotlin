package software.amazon.ionschema

/**
 * Represents an Ion Schema.
 */
interface Schema {
    /**
     * Returns the requested type, if present in this schema;
     * otherwise returns null.
     */
    fun getType(name: String): Type?

    /**
     * Returns an iterator over the types in this schema.
     */
    fun getTypes(): Iterator<Type>

    /**
     * Returns the IonSchemaSystem this schema was created by.
     */
    fun getSchemaSystem(): IonSchemaSystem
}

