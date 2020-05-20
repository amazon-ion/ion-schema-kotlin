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
                    val ch2 = si.next()
                    when (ch2) {
                        '[', ']', '\\' -> sb.append(ch2)
                        // not supporting pre-defined char classes (i.e., \d, \s, \w)
                        // as user is specifying a new char class
                        else -> error(si,
                                "invalid sequence '\\$ch2' in character class")
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
        throw InvalidSchemaException("$message in regex '$si' at offset ${si.currentIndex()}")
}

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

