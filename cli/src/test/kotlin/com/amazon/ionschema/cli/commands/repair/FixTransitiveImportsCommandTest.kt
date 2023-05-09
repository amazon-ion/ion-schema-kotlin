package com.amazon.ionschema.cli.commands.repair

import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.cli.IonSchemaCli
import com.amazon.ionschema.cli.commands.repair.TransitiveImportRewriter.ImportStrategy
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.stream.Stream

class FixTransitiveImportsCommandTest {

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class NoWildcards {
        @BeforeAll
        fun runCommand() = runCommand(ImportStrategy.NoWildcards)
        @TestFactory
        fun testCases() = testForStrategy(ImportStrategy.NoWildcards)
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class KeepWildcards {
        @BeforeAll
        fun runCommand() = runCommand(ImportStrategy.KeepWildcards)
        @TestFactory
        fun testCases() = testForStrategy(ImportStrategy.KeepWildcards)
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class PreferWildcards {
        @BeforeAll
        fun runCommand() = runCommand(ImportStrategy.PreferWildcards)
        @TestFactory
        fun testCases() = testForStrategy(ImportStrategy.PreferWildcards)
    }

    companion object {
        val ION = IonSystemBuilder.standard().build()
        private val testResourceDir = File("src/test/resources").canonicalPath
        private val baseOutputDir = File("build/tmp/test/").canonicalPath
        val timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS)

        private fun runCommand(strategy: ImportStrategy) {
            val outputDir = File("$baseOutputDir/FixTransitiveImportsCommand/$strategy-test-output-$timestamp")
            IonSchemaCli().parse(
                arrayOf(
                    "repair", "fix-transitive-imports",
                    "-s", "$strategy",
                    "-d", "$outputDir",
                    "$testResourceDir/FixTransitiveImportsCommand/input/",
                )
            )
        }

        fun testForStrategy(strategy: ImportStrategy): Stream<DynamicNode> {

            val outputDir = File("$baseOutputDir/FixTransitiveImportsCommand/$strategy-test-output-$timestamp")
            val expectationDir = File("$testResourceDir/FixTransitiveImportsCommand/output-$strategy")

            val testNodes = mutableListOf<DynamicNode>()
            expectationDir.walk()
                .filter { it.isFile }
                .forEach { expectedFile ->
                    val relativeFile = expectedFile.relativeTo(expectationDir)
                    val actualFile = outputDir.resolve(relativeFile)

                    // This should not be brittle, and should probably not require the expected outputs to be updated.
                    testNodes.add(
                        dynamicTest("Check $relativeFile are Ion equivalent", actualFile.toURI()) {
                            Assertions.assertEquals(
                                ION.loader.load(expectedFile),
                                ION.loader.load(actualFile),
                            )
                        }
                    )
                    // This test could be brittle, but we need it to ensure (a) that we don't wipe out any comments in the
                    // ISL, and (b) that we don't suddenly start making large style-related changes to the original files.
                    // For some changes this test may fail, and sometimes we might want to update the expected outputs
                    // (e.g. if there's some intended whitespace change).
                    // To do so, run:
                    // ./ion-schema-cli repair fix-transitive-imports -s <STRATEGY> \
                    //     cli/src/test/resources/FixTransitiveImportsCommand/input/ \
                    //     -d cli/src/test/resources/FixTransitiveImportsCommand/output-<STRATEGY>/
                    testNodes.add(
                        dynamicTest("Check $relativeFile are text equivalent", actualFile.toURI()) {
                            Assertions.assertEquals(
                                expectedFile.readText(),
                                actualFile.readText(),
                            )
                        }
                    )
                }
            return testNodes.stream()
        }
    }
}
