package com.amazon.ionschema.internal.util

import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.IonSchemaVersion

/**
 * Validates that a regex pattern is valid for the subset of ECMA-262 that is supported by Ion Schema, and converts
 * to the equivalent syntax for Java Regex.
 */
internal fun validateRegexPattern(string: String, islVersion: IonSchemaVersion = IonSchemaVersion.v2_0): String {
    val si = StringIterator(string)
    val sb = StringBuilder()
    var ch = si.next()
    do {
        when (ch) {
            '[' -> {
                sb.append(ch)
                parseCharacterClass(si, sb, islVersion)
            }
            '(' -> {
                sb.append(ch)
                ch = si.next()
                if (ch == '?') { // error on "(?..." constructs
                    error(si, "invalid character '$ch'")
                }
                sb.append(ch)
            }
            '\\' -> { // handle escaped chars
                ch = si.next()
                when (ch) {
                    '.', '^', '$', '|', '?', '*', '+', '\\',
                    '[', ']', '(', ')', '{', '}',
                    'w', 'W', 'd', 'D' -> sb.append('\\').append(ch)
                    's' -> sb.append("[ \\f\\n\\r\\t]")
                    'S' -> sb.append("[^ \\f\\n\\r\\t]")
                    else -> error(si, "invalid escape character '$ch'")
                }
            }
            else -> sb.append(ch) // otherwise, accept the character
        }

        parseQuantifier(si, sb) // parse a quantifier, if present

        ch = si.next()
    } while (ch != null)

    return sb.toString() // Pattern.compile(sb.toString(), flags)
}

private fun parseCharacterClass(si: StringIterator, sb: StringBuilder, islVersion: IonSchemaVersion) {
    do {
        val ch = si.next()
        sb.append(ch)

        when (ch) {
            '&' -> {
                if (si.peek() == '&') {
                    error(si, "'&&' is not supported in a character class")
                }
            }

            '[' -> error(si, "'[' must be escaped within a character class")

            '\\' -> {
                when (val ch2 = si.next()) {
                    '[', ']', '\\' -> sb.append(ch2)
                    'd', 's', 'w', 'D', 'S', 'W' -> if (islVersion == IonSchemaVersion.v1_0) {
                        // For Ion Schema 1.0, this is an error because ISL 1.0 does
                        // not support pre-defined char classes (i.e., \d, \s, \w)
                        // while user is specifying a new char class
                        error(si, "invalid sequence '\\$ch2' in character class")
                    } else {
                        // In Ion Schema 2.0, this is allowed
                        sb.append(ch2)
                    }
                    else -> error(
                        si,
                        "invalid sequence '\\$ch2' in character class"
                    )
                }
            }

            ']' -> return
        }
    } while (ch != null)

    error(si, "character class missing ']'")
}
private fun parseQuantifier(si: StringIterator, sb: StringBuilder) {
    val initialLength = sb.length
    var ch = si.peek()
    when (ch) {
        '?', '*', '+' -> {
            ch = si.next()
            sb.append(ch)
        }
        '{' -> {
            ch = si.next()
            sb.append(ch)
            var complete = false
            // A quantifier such as {,3} is not an ECMA 262 quantifier (it has no lower bound)
            // We track whether we've found a number so that we can ensure that a comma is only
            // allowed if it follows at least one digit.
            var foundAnyNumber = false
            do {
                ch = si.next()
                when (ch) {
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> { sb.append(ch); foundAnyNumber = true }
                    ',' -> if (foundAnyNumber) sb.append(ch) else error(si, "range quantifier is missing lower bound")
                    '}' -> {
                        sb.append(ch)
                        complete = true
                    }
                    null -> {}
                    else -> error(si, "invalid character '$ch'")
                }
            } while (ch != null && !complete)

            if (!complete) {
                error(si, "range quantifier missing '}'")
            }
        }
    }

    if (sb.length > initialLength && ch != null) {
        ch = si.peek()
        when (ch) {
            '?' -> error(si, "invalid character '$ch'")
            '+' -> error(si, "invalid character '$ch'")
        }
    }
}
private fun error(si: StringIterator, message: String): Unit =
    throw InvalidSchemaException("$message in regex '$si' at offset ${si.currentIndex()}")

private class StringIterator(private val s: String) {
    private var index = -1
    val length = s.length

    fun next(): Char? {
        index += 1
        return get(index)
    }

    fun peek() = get(index + 1)

    private fun get(i: Int): Char? {
        if (i < length) {
            return s[i]
        }
        return null
    }

    fun currentIndex() = index

    override fun toString() = s
}
