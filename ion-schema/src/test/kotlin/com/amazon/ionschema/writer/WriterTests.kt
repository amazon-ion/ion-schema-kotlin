// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer

import com.amazon.ion.IonList
import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ion.system.IonTextWriterBuilder
import com.amazon.ionschema.IonSchemaTests
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.TestFactory
import com.amazon.ionschema.asDocument
import com.amazon.ionschema.getTextField
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.reader.internal.IonSchemaReaderV2_0
import com.amazon.ionschema.reader.internal.VersionedIonSchemaReader
import com.amazon.ionschema.writer.internal.IonSchemaWriterV2_0
import com.amazon.ionschema.writer.internal.VersionedIonSchemaWriter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import java.io.File
import java.util.stream.Stream

@ExperimentalIonSchemaModel
class WriterTests {

    @Nested
    inner class IonSchema_2_0 : TestFactory by WriterTestsRunner(
        version = IonSchemaVersion.v2_0,
        reader = IonSchemaReaderV2_0,
        writer = IonSchemaWriterV2_0,
    )
}

@ExperimentalIonSchemaModel
class WriterTestsRunner(
    val version: IonSchemaVersion,
    val reader: VersionedIonSchemaReader,
    val writer: VersionedIonSchemaWriter,
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
            DynamicTest.dynamicTest("[$relativeFile] writeSchema should write a schema document that is equivalent to what was read") {
                val schema = reader.readSchemaOrThrow(dg)
                val newDg = ION.newDatagram()
                val ionWriter = ION.newWriter(newDg)
                writer.writeSchema(ionWriter, schema)
                val schema2 = reader.readSchemaOrThrow(newDg)
                assertEquals(schema, schema2)
            }

        val dynamicNodeTestCases = dg.mapNotNull { ion ->
            if (ion !is IonStruct) return@mapNotNull null

            when {
                ion.hasTypeAnnotation("type") -> {
                    val displayName = "[$relativeFile] writeNamedType '${ion.getTextField("name")}'"
                    DynamicTest.dynamicTest(displayName) {
                        val type = reader.readNamedTypeOrThrow(ion)
                        val stringBuilder = StringBuilder()
                        val ionWriter = IonTextWriterBuilder.standard().build(stringBuilder)
                        writer.writeNamedType(ionWriter, type)
                        println(stringBuilder)
                        val type2 = reader.readNamedTypeOrThrow(ION.singleValue(stringBuilder.toString()))
                        assertEquals(type, type2)
                    }
                }
                IonSchemaTests.isValidSchemasTestCase(ion) -> createSchemasTestCases("$relativeFile", ion)
                else -> null
            }
        }

        return DynamicContainer.dynamicContainer(
            relativeFile.path,
            f.toURI(),
            (dynamicNodeTestCases + validSchemaCase).stream().filter { testNameFilter(it.displayName) }
        )
    }

    private fun createSchemasTestCases(schemaId: String, ion: IonStruct): DynamicNode {
        val baseDescription = ion.getTextField("description")
        val cases = (ion["valid_schemas"] as IonList).mapIndexed { i, it ->
            DynamicTest.dynamicTest("[$schemaId] $baseDescription [$i]") {
                val dg = it.asDocument()
                val schema = reader.readSchemaOrThrow(dg)
                val newDg = ION.newDatagram()
                val ionWriter = ION.newWriter(newDg)
                writer.writeSchema(ionWriter, schema)
                val schema2 = reader.readSchemaOrThrow(newDg)
                assertEquals(schema, schema2)
            }
        }
        return DynamicContainer.dynamicContainer("[$schemaId] $baseDescription", cases)
    }
}
