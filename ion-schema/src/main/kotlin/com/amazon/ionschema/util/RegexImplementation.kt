// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.util

import java.util.function.Predicate
import java.util.regex.Pattern as JPattern

/**
 * Interface that allows any regular expression implementation to be injected into an
 * [`IonSchemaSystem`][com.amazon.ionschema.IonSchemaSystem].
 *
 * See [`IonSchemaSystemBuilder.withRegexImplementation`]
 * [com.amazon.ionschema.IonSchemaSystemBuilder.withRegexImplementation] for details.
 */
interface RegexImplementation {

    /** Compile a [pattern] string into a [Pattern]. */
    fun compile(pattern: String, multiline: Boolean, caseInsensitive: Boolean): Pattern

    /** An abstraction over a compiled regular expression regardless of the particular regex implementation. */
    open class Pattern(
        /** The regular expression from which this pattern was compiled */
        val pattern: String,
        /** A predicate which can be used for finding a match on a subsequence of a string. */
        test: Predicate<String>
    ) : Predicate<String> by test
}

/** Default [RegexImplementation] used by Ion Schema Kotlin. This is backed by the Java standard library. */
object DefaultRegexImplementation : RegexImplementation {

    override fun compile(pattern: String, multiline: Boolean, caseInsensitive: Boolean): RegexImplementation.Pattern {
        val flags = (if (multiline) JPattern.MULTILINE else 0) +
            (if (caseInsensitive) JPattern.CASE_INSENSITIVE else 0)

        return RegexImplementation.Pattern(pattern, JPattern.compile(pattern, flags).asPredicate())
    }
}
