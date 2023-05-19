package com.amazon.ionschema.reader

import com.amazon.ion.IonBool
import com.amazon.ion.IonList
import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.IonSchemaTests
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.TestFactory
import com.amazon.ionschema.asDocument
import com.amazon.ionschema.getTextField
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.util.stream.Stream

@ExperimentalIonSchemaModel
class ReaderTests {

    val unimplementedConstraints = listOf(
        "annotations",
        "contains",
        "timestamp_offset",
        "timestamp_precision",
        "valid_values",
    )
    val unimplementedConstraintsRegex = Regex("constraints/(${unimplementedConstraints.joinToString("|")})")

    @Nested
    inner class IonSchema_2_0 : TestFactory by ReaderTestsRunner(
        version = IonSchemaVersion.v2_0,
        reader = IonSchemaReaderV2_0(),
        additionalFileFilter = {
            !it.path.contains(unimplementedConstraintsRegex) &&
                !it.path.contains("schema/") &&
                !it.path.contains("imports/") &&
                !it.path.contains("open_content/")
        },
        testNameFilter = {
            // readSchema() is not implemented yet
            !it.contains("readSchema")
        }
    )
}

@ExperimentalIonSchemaModel
class ReaderTestsRunner(
    val version: IonSchemaVersion,
    val reader: IonSchemaReader,
    additionalFileFilter: (File) -> Boolean = { true },
    private val testNameFilter: (String) -> Boolean = { true },
) : TestFactory {

    companion object {
        private val ION = IonSystemBuilder.standard().build()
    }

    private val fileFilter: (File) -> Boolean = { it.path.endsWith(".isl") && additionalFileFilter(it) }
    private val baseDir = IonSchemaTests.testDirectoryFor(version)

    override fun generateTests(): Iterable<DynamicNode> {
        return baseDir.walk()
            .filter { it.isFile }
            .filter(fileFilter)
            .map { generateTestCases(it) }
            .asIterable()
    }

    private fun generateTestCases(f: File): DynamicNode {
        val relativeFile = f.relativeTo(baseDir)
        val dg = ION.loader.load(f)

        val fileIslVersion = dg[0].takeIf { IonSchemaVersion.isVersionMarker(it) }
            ?.let { IonSchemaVersion.fromIonSymbolOrNull(it as IonSymbol) }
            ?.takeIf { it == IonSchemaVersion.v2_0 }
            ?: IonSchemaVersion.v1_0

        if (fileIslVersion != IonSchemaVersion.v2_0) return DynamicContainer.dynamicContainer(relativeFile.path, f.toURI(), Stream.empty())

        val validSchemaCase =
            DynamicTest.dynamicTest("[$relativeFile] readSchema should read a valid schema document") {
                reader.readSchemaOrThrow(dg)
            }

        val dynamicNodeTestCases = dg.mapNotNull { ion ->
            // The reader is a little more sophisticated than ISL for ISL, but it cannot detect problems with
            // duplicate type names or imports.
            if (ion !is IonStruct || !ion.islForIslCanValidate()) return@mapNotNull null

            when {
                ion.hasTypeAnnotation("type") -> {
                    val displayName = "[$relativeFile] readNamedType '${ion.getTextField("name")}'"
                    DynamicTest.dynamicTest(displayName) { reader.readNamedTypeOrThrow(ion) }
                }
                IonSchemaTests.isInvalidTypesTestCase(ion) -> {
                    val baseDescription = ion.getTextField("description")
                    val cases = (ion["invalid_types"] as IonList).mapIndexed { i, invalidType ->
                        val displayName = "[$relativeFile] $baseDescription [$i]"
                        DynamicTest.dynamicTest(displayName) {
                            assertThrows<InvalidSchemaException> { reader.readTypeOrThrow(invalidType) }
                        }
                    }
                    DynamicContainer.dynamicContainer("[$relativeFile] $baseDescription", cases)
                }

                IonSchemaTests.isInvalidSchemasTestCase(ion) -> {
                    createSchemasTestCases("$relativeFile", ion, expectValid = false)
                }
                IonSchemaTests.isValidSchemasTestCase(ion) -> {
                    createSchemasTestCases("$relativeFile", ion, expectValid = true)
                }
                else -> null
            }
        }

        return DynamicContainer.dynamicContainer(
            relativeFile.path,
            f.toURI(),
            (dynamicNodeTestCases + validSchemaCase).stream().filter { testNameFilter(it.displayName) }
        )
    }

    private fun createSchemasTestCases(schemaId: String, ion: IonStruct, expectValid: Boolean): DynamicNode {
        val baseDescription = ion.getTextField("description")
        val schemasField = if (expectValid) "valid_schemas" else "invalid_schemas"
        val cases = (ion[schemasField] as IonList).mapIndexed { i, it ->
            DynamicTest.dynamicTest("[$schemaId] $baseDescription [$i]") {
                if (expectValid)
                    reader.readSchemaOrThrow(it.asDocument()) // Asserts nothing is thrown
                else
                    assertThrows<InvalidSchemaException> { reader.readSchemaOrThrow(it.asDocument()) }
            }
        }
        return DynamicContainer.dynamicContainer("[$schemaId] $baseDescription", cases)
    }

    private fun IonStruct.islForIslCanValidate(): Boolean {
        return (this["isl_for_isl_can_validate"] as? IonBool)?.booleanValue() ?: true
    }
}
