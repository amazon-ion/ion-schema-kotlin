package software.amazon.ionschema.internal

/**
 * Marker interface for the non-scalar and non-container types defined
 * in the Ion Schema Specification--types that are defined in [SchemaCore]
 * by referring to other Core/Ion types.
 *
 * Builtin types include 'lob', 'number', and 'any', but not 'document'.
 */
internal interface TypeBuiltin : TypeInternal

