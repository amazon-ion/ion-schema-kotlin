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

import com.amazon.ionschema.reader.internal.ReadError

/**
 * Thrown when an invalid schema definition is encountered.
 */
class InvalidSchemaException(message: String) : IonSchemaException(message) {

    /**
     * Indicates whether this exception is a fail-fast exception (i.e. it should not be caught by any error collectors
     * in an Ion Schema reader).
     */
    private var failFast = false
    internal fun isFailFast() = failFast

    companion object {
        /**
         * Factory function to create and throw a fail-fast [IonSchemaException] from a [ReadError].
         */
        internal fun failFast(error: ReadError): Nothing {
            val e = InvalidSchemaException(error.message)
            e.failFast = true
            throw e
        }
    }
}
