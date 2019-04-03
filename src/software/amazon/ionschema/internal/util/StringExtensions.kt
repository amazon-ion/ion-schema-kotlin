package software.amazon.ionschema.internal.util

/**
 * String extension functions
 */
internal fun String.truncate(limit: Int, truncated: CharSequence = "..."): String {
    if (length < limit) {
        return this
    }
    return substring(0, limit) + truncated
}

