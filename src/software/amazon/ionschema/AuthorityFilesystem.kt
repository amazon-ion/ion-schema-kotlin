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

package software.amazon.ionschema

import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.IonSchemaSystemImpl
import software.amazon.ionschema.util.CloseableIterator
import java.io.File
import java.io.FileReader
import java.io.Reader

/**
 * An [Authority] implementation that attempts to resolve schema ids to files
 * relative to a basePath.
 *
 * @property[basePath] The base path in the filesystem in which to resolve schema identifiers.
 */
class AuthorityFilesystem(private val basePath: String) : Authority {
    init {
        if (!File(basePath).exists()) {
            throw IllegalArgumentException("Path '$basePath' does not exist")
        }
    }

    override fun iteratorFor(iss: IonSchemaSystem, id: String): CloseableIterator<IonValue> {
        val file = File(basePath, id)
        if (file.exists() && file.canRead()) {
            return object : CloseableIterator<IonValue> {
                private var reader: FileReader? = FileReader(file)
                private val iter = (iss as IonSchemaSystemImpl).getIonSystem().iterate(reader)

                override fun hasNext() = iter.hasNext()
                override fun next() = iter.next()
                override fun close() {
                    try {
                        reader?.let(Reader::close)
                    } finally {
                        reader = null
                    }
                }
            }
        }
        return EMPTY_ITERATOR
    }
}

