/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import com.amazon.ion.system.IonSystemBuilder
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ResourceAuthorityTest {

    @Test
    fun unknownSchemaId() {
        val iss = IonSchemaSystemBuilder.standard().build()
        val authority = ResourceAuthority("ion-schema-schemas", ResourceAuthority::class.java.classLoader)
        val iter = authority.iteratorFor(iss, "unknown_schema_id")
        assertFalse(iter.hasNext())
        try {
            iter.next()
            fail()
        } catch (e: NoSuchElementException) {
        }
        iter.close()
    }

    @Test
    fun knownSchemaId() {
        val iss = IonSchemaSystemBuilder.standard().build()
        val authority = ResourceAuthority("ion-schema-schemas", ResourceAuthority::class.java.classLoader)
        val iter = authority.iteratorFor(iss, "isl/schema.isl")
        assertTrue(iter.hasNext())
        iter.close()
    }

    @Test
    fun schemaIdOutsideBasePath() {
        val iss = IonSchemaSystemBuilder.standard().build()
        val authority = ResourceAuthority("ion-schema-schemas", ResourceAuthority::class.java.classLoader)
        try {
            authority.iteratorFor(iss, "../outside_the_base_path")
            fail()
        } catch (e: AccessDeniedException) {
            // Pass
        }
    }

    @Test
    fun canLoadIonSchemaSchemas() {
        val ion = IonSystemBuilder.standard().build()
        val iss = IonSchemaSystemBuilder.standard()
            .withIonSystem(ion)
            .withAuthority(ResourceAuthority.forIonSchemaSchemas())
            .build()
        val islSchema = iss.loadSchema("isl/schema.isl")

        assertNotNull("Unable to find the schema for 'schema'", islSchema.getType("schema"))
    }
}
