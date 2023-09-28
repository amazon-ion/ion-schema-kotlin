// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.model

/**
 * The collection of field names that are reserved by the user as open content fields.
 * See relevant section in [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#open-content).
 */
data class UserReservedFields(
    val type: Set<String> = emptySet(),
    val header: Set<String> = emptySet(),
    val footer: Set<String> = emptySet(),
) {
    companion object {
        @JvmStatic
        val EMPTY = UserReservedFields()
    }
}
