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

import org.junit.Assert.assertFalse
import org.junit.Assert.fail
import org.junit.Test

class AuthorityFilesystemTest {
    @Test(expected = IllegalArgumentException::class)
    fun nonExistentPath() {
        AuthorityFilesystem("non-existent-path")
    }

    @Test
    fun unknownSchemaId() {
        val iss = IonSchemaSystemBuilder.standard().build()
        val authority = AuthorityFilesystem("ion-schema-tests")
        val iter = authority.iteratorFor(iss, "unknown_schema_id")
        assertFalse(iter.hasNext())
        try {
            iter.next()
            fail()
        } catch (e: NoSuchElementException) {
        }
        iter.close()
    }
}

