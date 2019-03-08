package software.amazon.ionschema.internal.util

/**
 * String extension functions
 */
internal fun String.truncate(limit: Int, truncated: CharSequence = "..."): String {
    if (this.length < limit) {
        return this
    }
    return this.substring(0, limit) + truncated
}

