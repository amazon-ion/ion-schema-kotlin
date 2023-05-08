package com.amazon.ionschema.model

/**
 * The collection of field names that are reserved by the user as open content fields.
 * See relevant section in [ISL 2.0 spec](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#open-content).
 */
data class UserReservedFields(
    val type: List<String> = emptyList(),
    val header: List<String> = emptyList(),
    val footer: List<String> = emptyList(),
)