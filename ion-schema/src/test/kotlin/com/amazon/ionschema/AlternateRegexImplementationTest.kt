// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema

import com.amazon.ionschema.util.RegexImplementation
import com.google.re2j.Pattern

/**
 * Alternate Regex implementation that promises linear time evaluation.
 *
 * See [`google/re2j`](https://github.com/google/re2j/) on GitHub for more information.
 *
 * Use with caution! The `re2j` library is not quite compliant to Ion Schema Specification. Specifically, it does not
 * treat `\r`, `\u2028`, and `\u2029` as newline characters. For many applications, this is fine because *nix systems
 * use `\n`. If you are validating data from a Windows system, you may encounter issues because Windows uses `\r\n` for
 * a newline.
 *
 * A naive workaround would be to replace all occurrences of the `.` character class with `[^\n\r\u2028\u2029]` in
 * the regular expression before compiling it. However, that doesn't work when the multiline flag is set because `^` and
 * `$` still do not match any newline characters other than `\n`.
 *
 * Another workaround (and the one that this class optionally supports) is to pre-process the regex pattern and the
 * regex input to replace all `\r`, `\u2028`, and `\u2029` with `\n`. This approach is _probably_ sufficient for any
 * use case that doesn't try to distinguish between different newline characters.
 */
class Re2jRegexImplementation(private val consolidateNewlines: Boolean = false) : RegexImplementation {

    override fun compile(pattern: String, multiline: Boolean, caseInsensitive: Boolean): RegexImplementation.Pattern {
        val flags = (if (multiline) Pattern.MULTILINE else 0) +
            (if (caseInsensitive) Pattern.CASE_INSENSITIVE else 0)

        val compiled = if (consolidateNewlines) {
            Pattern.compile(pattern.consolidateNewlines(), flags)
        } else {
            Pattern.compile(pattern, flags)
        }

        return RegexImplementation.Pattern(pattern) { input ->
            if (consolidateNewlines) {
                compiled.matcher(input.consolidateNewlines()).find()
            } else {
                compiled.matcher(input).find()
            }
        }
    }

    /**
     * Consolidates all newline characters to be the same by replacing all `\r`, `\u2028`, and `\u2029` with `\n`
     */
    private fun String.consolidateNewlines() = map { consolidateNewlineChars(it) }.joinToString("")

    /** Returns `\n` if [c] is any newline character, otherwise returns [c] */
    private fun consolidateNewlineChars(c: Char) = when (c) {
        '\r', '\u2028', '\u2029' -> '\n'
        else -> c
    }
}

class IonSchemaTests_1_0_AlternateRegex : TestFactory by IonSchemaTestsRunner(
    islVersion = IonSchemaVersion.v1_0,
    systemBuilder = IonSchemaSystemBuilder.standard()
        .allowTransitiveImports(false)
        // Some cases in ion-schema-tests use \r, so we need to consolidate the newlines.
        .withRegexImplementation(Re2jRegexImplementation(consolidateNewlines = true))
)

class IonSchemaTests_2_0_AlternateRegex : TestFactory by IonSchemaTestsRunner(
    islVersion = IonSchemaVersion.v2_0,
    systemBuilder = IonSchemaSystemBuilder.standard()
        .allowTransitiveImports(false)
        // Some cases in ion-schema-tests use \r, so we need to consolidate the newlines.
        .withRegexImplementation(Re2jRegexImplementation(consolidateNewlines = true)),
    // This one test fails because it's checking to make sure that '\n' and '\r' aren't interchangeable
    testNameFilter = {
        it != "[constraints/regex.isl] Type 'regex_unescaped_newline' should not match value: \"hello\\rworld\""
    }
)
