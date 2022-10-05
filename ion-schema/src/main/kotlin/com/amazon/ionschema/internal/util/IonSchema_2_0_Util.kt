package com.amazon.ionschema.internal.util

/**
 * See https://amzn.github.io/ion-schema/docs/isl-2-0/spec#reserved-symbols
 */
internal val ISL_2_0_RESERVED_WORDS_REGEX = Regex("(\\\$ion_schema(_.*)?|[a-z][a-z\\d]*(_[a-z\\d]+)*)")
