/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

/**
 * An Import represents all the types imported by a Schema
 * from one schema id.
 *
 * Note that multiple ISL imports referencing the same schema id
 * (each importing/aliasing an individual type) are represented by a
 * single Import instance.
 */
interface Import {
    /**
     * The schema id referenced by the import.
     */
    val id: String

    /**
     * Returns the requested type, if present in this import;
     * otherwise returns null.  If a type is aliased (via "as")
     * by the import, the type will only be returned from this
     * method by its alias (not the imported type's original name).
     */
    fun getType(name: String): Type?

    /**
     * Returns an iterator over the types imported by this Import.
     */
    fun getTypes(): Iterator<Type>
}

