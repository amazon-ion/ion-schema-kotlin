/*
 * Copyright 2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
 * Contains utilities for working with [Ion Schema Schemas](https://github.com/amazon-ion/ion-schema-schemas).
 */
object IonSchemaSchemas {

    @JvmStatic
    private val AUTHORITY = ResourceAuthority("ion-schema-schemas", IonSchemaSchemas.javaClass.classLoader)

    /**
     * Returns an [Authority] implementation that provides [Ion Schema Schemas](https://github.com/amazon-ion/ion-schema-schemas).
     */
    @JvmStatic
    fun authority(): Authority = AUTHORITY

    /**
     * Schema ID to use to load the schema for a specific version of the Ion Schema language. The schema at this ID
     * has the types, `schema`, `schema_header`, `schema_footer`, `named_type_definition`, and `inline_type_definition`
     * which can be used to validate fragments of ISL or a whole Ion Schema document.
     */
    @JvmStatic
    fun getSchemaIdForIslVersion(version: IonSchemaVersion) = when (version) {
        IonSchemaVersion.v1_0 -> "isl/ion_schema_1_0.isl"
        IonSchemaVersion.v2_0 -> "isl/ion_schema_2_0.isl"
    }
}
