/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package com.amazon.ionschema.model

import com.amazon.ionelement.api.IonElement

/**
 * Represents the header of a schema document.
 */
data class AstHeader(val imports: List<Import>, val additionalContent: Map<String, List<IonElement>> = emptyMap()) {

    companion object {
        @JvmField
        val EMPTY = AstHeader(emptyList())
    }

    sealed class Import {
        /**
         * Represents an import statement for all types from the given [schemaId].
         */
        data class SchemaImport(val schemaId: String) : Import()

        /**
         * Represents an import statement for a specific [typeId] from the given [schemaId]. The [alias] is the
         * name by which the type can be referenced in the schema that this header is associated with.
         */
        data class TypeImport(val schemaId: String, val typeId: String, val alias: String = typeId) : Import()
    }
}
