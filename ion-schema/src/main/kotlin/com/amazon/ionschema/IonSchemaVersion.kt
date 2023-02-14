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

package com.amazon.ionschema

import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

enum class IonSchemaVersion(val symbolText: String) {
    v1_0("\$ion_schema_1_0"),
    v2_0("\$ion_schema_2_0");

    companion object {
        internal fun fromIonSymbolOrNull(symbol: IonSymbol): IonSchemaVersion? = values().singleOrNull { it.symbolText == symbol.stringValue() }

        /**
         * Tests if the IonValue is a value that is reserved for version markers, as per
         * [ISL Versioning](https://amazon-ion.github.io/ion-schema/docs/isl-versioning#ion-schema-version-markers).
         */
        @OptIn(ExperimentalContracts::class)
        internal fun isVersionMarker(value: IonValue): Boolean {
            contract { returns(true) implies (value is IonSymbol) }
            return value is IonSymbol && !value.isNullValue && IonSchemaVersion.VERSION_MARKER_REGEX.matches(value.stringValue())
        }

        @JvmStatic
        private val VERSION_MARKER_REGEX = Regex("^\\\$ion_schema_\\d.*")
    }
}
