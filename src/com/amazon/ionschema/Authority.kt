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

import com.amazon.ion.IonValue
import com.amazon.ionschema.util.CloseableIterator

/**
 * An Authority is responsible for resolving a particular class of
 * schema identifiers.
 *
 * The structure of a schema identifier string is defined by the
 * Authority responsible for the schema/type(s) being imported.
 *
 * **Runtime resolution of a schema over a network presents availability and security risks, and should thereby be avoided.**
 *
 * @see AuthorityFilesystem
 */
interface Authority {
    /**
     * Provides a CloseableIterator<IonValue> for the requested schema identifier.
     * If an error condition is encountered while attempting to resolve the schema
     * identifier, this method should throw an exception.  If no error conditions
     * were encountered, but the schema identifier can't be resolved, this method
     * should return [EMPTY_ITERATOR].
     */
    fun iteratorFor(iss: IonSchemaSystem, id: String): CloseableIterator<IonValue>
}

/**
 * A singleton iterator which has nothing to iterate over.
 */
val EMPTY_ITERATOR = object : CloseableIterator<IonValue> {
    override fun hasNext() = false
    override fun next(): IonValue = throw NoSuchElementException()
    override fun close() { }
}

