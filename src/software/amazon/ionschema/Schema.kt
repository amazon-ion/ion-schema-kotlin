package software.amazon.ionschema

import software.amazon.ion.IonStruct

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

    /**
     * Constructs a new type using the type ISL provided as a string.
     *
     * @param[isl] ISL string representation of the type to create
     * @return the new type
     */
    fun newType(isl: String): Type

    /**
     * Constructs a new type using the type ISL provided as an IonStruct.
     *
     * @param[isl] IonStruct representing the desired type
     * @return the new type
     */
    fun newType(isl: IonStruct): Type
}

