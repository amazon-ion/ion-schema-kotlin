/*
 * Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazon.ionschema

import com.amazon.ion.IonDatagram
import com.amazon.ion.IonStruct

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
     * A read-only view of the ISL for this schema.
     */
    val isl: IonDatagram

    /**
     * Returns an Import representing all the types imported from
     * the specified schema [id].
     */
    fun getImport(id: String): Import?

    /**
     * Returns an iterator over the imports of this Schema.  Note that
     * multiple ISL imports referencing the same schema id (to import
     * individual types from the same schema id, for example) are
     * represented by a single Import object.
     */
    fun getImports(): Iterator<Import>

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
     * Returns a new Schema instance containing all the types of this
     * instance plus the provided type.  Note that the added type
     * in the returned instance will hide a type of the same name
     * from this instance.
     */
    fun plusType(type: Type): Schema
}
