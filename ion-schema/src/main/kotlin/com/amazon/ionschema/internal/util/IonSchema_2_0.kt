package com.amazon.ionschema.internal.util

import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.internal.ConstraintFactoryDefault

/**
 * A collection of utilities that are specific to Ion Schema 2.0. This doesn't have to be an object, but wrapping
 * these functions in an object conveniently namespaces them so that they can be referred to as [IonSchema_2_0.KEYWORDS], etc.
 */
internal object IonSchema_2_0 {
    /**
     * Keywords that are valid to use in a schema header
     */
    val HEADER_KEYWORDS = setOf("imports", "user_reserved_fields")

    /**
     * Keywords that could be valid to use in a type definition. Not all of these are valid in all type definitions.
     * For example, `name` can only occur in top-level type definitions.
     */
    val TYPE_KEYWORDS = ConstraintFactoryDefault.getConstraintNamesForVersion(IonSchemaVersion.v2_0) + setOf("name", "occurs")

    /**
     * Keywords that are valid in an import.
     */
    val IMPORT_KEYWORDS = setOf("id", "type", "as")

    /**
     * Keywords that are valid as annotations on top-level types.
     */
    val TOP_LEVEL_ANNOTATION_KEYWORDS = setOf("schema_header", "schema_footer", "type")

    /**
     * All Ion Schema keywords.
     */
    val KEYWORDS = TOP_LEVEL_ANNOTATION_KEYWORDS + HEADER_KEYWORDS + TYPE_KEYWORDS + IMPORT_KEYWORDS

    /**
     * Regex to match symbols that are reserved in the Ion Schema specification.
     * See https://amzn.github.io/ion-schema/docs/isl-2-0/spec#reserved-symbols
     */
    val RESERVED_WORDS_REGEX = Regex("(\\\$ion_schema(_.*)?|[a-z][a-z\\d]*(_[a-z\\d]+)*)")
}
