/*
 * Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.ionschema.internal.constraint

import com.amazon.ion.IonString
import com.amazon.ion.IonText
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.Violations
import com.amazon.ionschema.Violation
import java.util.regex.Pattern

/**
 * Implements the regex constraint.  This implementation translates
 * the regex features defined by the Ion Schema Specification to
 * Java's Pattern class, and makes a best-effort to error if the
 * caller tries to use a feature of Pattern that is NOT defined
 * in the Ion Schema Specification.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#regex
 * @see java.util.regex.Pattern
 */
internal class Regex(
        ion: IonValue
) : ConstraintBase(ion) {

    private val pattern: Pattern

    init {
        if (ion !is IonString || ion.isNullValue) {
            throw InvalidSchemaException("Invalid regex constraint: $ion")
        }

        var flags = 0
        ion.typeAnnotations.forEach {
            val flag = when (it) {
                "i" -> Pattern.CASE_INSENSITIVE
                "m" -> Pattern.MULTILINE
                else -> throw InvalidSchemaException(
                        "Unrecognized flags for regex ($ion)")
            }
            flags = flags.or(flag)
        }

        pattern = toPattern(ion.stringValue(), flags)
    }

    override fun validate(value: IonValue, issues: Violations) {
        validateAs<IonText>(value, issues) { v ->
            if (!pattern.matcher(v.stringValue()).find()) {
                issues.add(Violation(ion, "regex_mismatch",
                        "'${v.stringValue()}' doesn't match regex '${pattern.pattern()}'"))
            }
        }
    }

    private fun toPattern(string: String, flags: Int): Pattern {
        val si = StringIterator(string)
        val sb = StringBuilder()
        var ch = si.next()
        do {
            when (ch) {
                // Ion escape characters
                '\u0000' -> sb.append("\\u0000")    // \0
                '\u0007' -> sb.append("\\u0007")    // \a
                '\u0008' -> sb.append("\\u0008")    // \b
                '\u0009' -> sb.append("\\u0009")    // \t
                '\u000A' -> sb.append("\\u000A")    // \n
                '\u000B' -> sb.append("\\u000B")    // \v
                '\u000C' -> sb.append("\\u000C")    // \f
                '\u000D' -> sb.append("\\u000D")    // \r

                '[' -> {
                    sb.append(ch)
                    parseCharacterClass(si, sb)
                }
                '(' -> {
                    sb.append(ch)
                    ch = si.next()
                    if (ch == '?') {            // error on "(?..." constructs
                        error(si, "invalid character '$ch'")
                    }
                    sb.append(ch)
                }
                '\\' -> {                       // handle escaped chars
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
                else -> sb.append(ch)           // otherwise, accept the character
            }

            parseQuantifier(si, sb)   // parse a quantifier, if present

            ch = si.next()
        } while (ch != null)

        return Pattern.compile(sb.toString(), flags)
    }

    private fun parseCharacterClass(si: StringIterator, sb: StringBuilder) {
        var ch = si.next()
        sb.append(ch)

        if (ch == '^') {
            ch = si.next()
            sb.append(ch)
        }

        ch = si.next()
        if (ch == '\\') {      // block use of Pattern's "[\p..." constructs
            error(si, "invalid character '$ch' in character class")
        }
        sb.append(ch)

        if (ch == '-') {       // it's a character range
            ch = si.next()
            sb.append(ch)

            ch = si.next()
            when (ch) {
                ']' -> sb.append(ch)
                null -> error(si, "unexpected end of string")
                else -> error(si, "unexpected character '$ch'")
            }

        } else {               // it's a character class
            var complete = false
            ch = si.next()
            while (ch != null && !complete) {
                sb.append(ch)
                if (ch == ']') {
                    complete = true
                } else {
                    ch = si.next()
                }
            }

            if (!complete) {
                error(si, "character class missing ']'")
            }
        }
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
                do {
                    ch = si.next()
                    when (ch) {
                        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ',' -> sb.append(ch)
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
        throw InvalidSchemaException("$message in regex '$si' at offset ${si.index}")
}

private class StringIterator(private val s: String) {
    var index = -1
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

    override fun toString() = s
}

