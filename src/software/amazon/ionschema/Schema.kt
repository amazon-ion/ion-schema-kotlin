package software.amazon.ionschema

import software.amazon.ion.IonStruct

/**
 * A Schema is a collection of zero or more [Type]s.
 *
 * Each type may refer to other types within the same schema,
 * or types imported into this schema from other schemas.
 * To instantiate a Schema, see [IonSchemaSystem].
 *
 * Classes that implement this interface are expected to be
 * immutable.  This avoids surprising behavior, for instance:
 * if a particular type in a schema were allowed to be replaced,
 * a value that was once valid for the type may no longer be valid.
 * Instead, any methods that would mutate a Schema are expected
 * to return a new Schema instance with the mutation applied
 * (see [plusType] as an example of this).
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
     * Constructs a new type using the type ISL provided as a String.
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

    /**
     * Constructs a new type using the type ISL provided as a String,
     * and returns a new Schema instance containing all the types
     * of this instance plus the new type.  Note that the new type
     * in the returned instance will hide a type of the same name
     * from this instance.
     */
    fun plusType(isl: String): Schema

    /**
     * Constructs a new type using the type ISL provided as an IonStruct.
     * and returns a new Schema instance containing all the types
     * of this instance plus the new type.  Note that the new type
     * in the returned instance will hide a type of the same name
     * from this instance.
     */
    fun plusType(isl: IonStruct): Schema
}

