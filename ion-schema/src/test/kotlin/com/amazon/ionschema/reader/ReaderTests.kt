package com.amazon.ionschema.reader

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

    val ISL_2_0_EXPECTED_TO_NOT_PASS = setOf(
        // These test cases are skipped because the reader doesn't try to resolve any type references or imports
        "[constraints/all_of.isl] all_of type references should exist",
        "[constraints/any_of.isl] any_of type references should exist",
        "[constraints/one_of.isl] one_of type references should exist",
        "[constraints/annotations-standard.isl] annotations argument type must exist",
        "[imports/diamond/inline_import_a.isl] no imported types should be available in this schema's scope",
        "[imports/diamond/header_import_a.isl] no indirectly imported types should be available in this schema's scope",
        "[imports/tree/inline_import_a.isl] no imported types should be available in this schema's scope",
        "[imports/tree/header_import_a.isl] no indirectly imported types should be available in this schema's scope",
        "[imports/invalid_imports.isl] when imported schema or type does not exist, should be an invalid schema",
        "[imports/header_imports.isl] A schema should not be able to reference an aliased type by its original name",
        "[imports/header_imports.isl] Two different imported types with the same name or alias should result in an error",
        "[imports/header_imports.isl] Importing a type with the same name or alias as locally defined type should result in an error",
        "[imports/self_import/self_import.isl] Self-imports are invalid",
        // TODO: Remove once timestamp precision range is fixed https://github.com/amazon-ion/ion-schema-kotlin/issues/270
        "[constraints/timestamp_precision.isl] timestamp_precision range must be satisfiable",
    )

    @Nested
    inner class IonSchema_2_0 : TestFactory by ReaderTestsRunner(
        version = IonSchemaVersion.v2_0,
        reader = IonSchemaReaderV2_0(),
        testNameFilter = { it !in ISL_2_0_EXPECTED_TO_NOT_PASS }
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
            if (ion !is IonStruct) return@mapNotNull null

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
                            assertThrows<InvalidSchemaException>("invalid type: $invalidType") { reader.readTypeOrThrow(invalidType) }
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
}
