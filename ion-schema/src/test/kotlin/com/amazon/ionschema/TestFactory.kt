package com.amazon.ionschema

import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory

/**
 * Interface-based alternative to [org.junit.jupiter.api.TestFactory] so that we can create test classes by composition/delegation.
 */
internal interface TestFactory {
    @TestFactory
    fun generateTests(): Iterable<DynamicNode>
}
