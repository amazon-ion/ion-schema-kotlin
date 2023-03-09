/*
 * Copyright 2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.ionschema

import com.amazon.ion.IonBool
import com.amazon.ion.IonList
import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.IonSchemaSchemasTestsRunner.Companion.ION_SCHEMA_SCHEMAS_DIR
import com.amazon.ionschema.IonSchemaTests.isInvalidSchemasTestCase
import com.amazon.ionschema.IonSchemaTests.isInvalidTypesTestCase
import com.amazon.ionschema.IonSchemaTests.isValidSchemasTestCase
import com.amazon.ionschema.IonSchemaTests.testDirectoryFor
import com.amazon.ionschema.internal.IonSchemaSystemImpl
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import java.io.File

class IslForIslTests {
    @Nested
    inner class IonSchemaTests_1_0 :
        TestFactory by IonSchemaSchemasTestsRunner(testDirectoryFor(IonSchemaVersion.v1_0))

    @Nested
    inner class IonSchemaTests_2_0 :
        TestFactory by IonSchemaSchemasTestsRunner(testDirectoryFor(IonSchemaVersion.v2_0))

    @Nested
    inner class IonSchemaSchemas :
        TestFactory by IonSchemaSchemasTestsRunner(ION_SCHEMA_SCHEMAS_DIR)
}

class IonSchemaSchemasTestsRunner(private val baseDir: File) : TestFactory {

    companion object {
        operator fun invoke(baseDir: String) = IonSchemaSchemasTestsRunner(File(baseDir))

        const val ION_SCHEMA_SCHEMAS_DIR = "../ion-schema-schemas"
    }

    private val ION = IonSystemBuilder.standard().build()

    private val schemaSystem = IonSchemaSystemBuilder.standard()
        .withIonSystem(ION)
        .withAuthority(IonSchemaSchemas.authority())
        .allowTransitiveImports(false)
        .build() as IonSchemaSystemImpl

    private val anyVersionOfSchema = schemaSystem.loadSchema("isl/ion_schema.isl").requireType("schema")

    override fun generateTests(): Iterable<DynamicNode> {
        return baseDir.walk()
            .filter { it.isFile && it.path.endsWith(".isl") }
            .map { generateTestCases(it) }
            .asIterable()
    }

    private fun Schema.requireType(name: String) = requireNotNull(getType(name)) { "Unable to get type '$name' for $ionSchemaLanguageVersion" }

    private fun generateTestCases(f: File): DynamicNode {
        val relativeLocation = f.relativeTo(baseDir)
        val dg = ION.loader.load(f)

        val validSchemaCase =
            DynamicTest.dynamicTest("[$relativeLocation] the test file should be a valid schema document") {
                anyVersionOfSchema.assertValid(dg,)
            }

        val fileIslVersion = dg.firstOrNull { it is IonSymbol && it.stringValue().matches(Regex("^\\\$ion_schema_\\d.*")) }
            ?.let { IonSchemaVersion.fromIonSymbolOrNull(it as IonSymbol) } ?: IonSchemaVersion.v1_0

        val specificIslVersion = schemaSystem.loadSchema(IonSchemaSchemas.getSchemaIdForIslVersion(fileIslVersion))
        val namedTypeDefinition = specificIslVersion.requireType("named_type_definition")
        val inlineTypeDefinition = specificIslVersion.requireType("inline_type_definition")
        val schemaType = specificIslVersion.requireType("schema")
        val headerType = specificIslVersion.requireType("schema_header")
        val footerType = specificIslVersion.requireType("schema_footer")

        val dynamicNodeTestCases = dg.mapNotNull { ion ->
            when {
                ion !is IonStruct -> null
                ion.hasTypeAnnotation("type") -> {
                    val displayName = "[$relativeLocation] ${ion.getTextField("name")}"
                    DynamicTest.dynamicTest(displayName) { namedTypeDefinition.assertValid(ion) }
                }
                ion.hasTypeAnnotation("schema_header") -> {
                    val displayName = "[$relativeLocation] schema_header should be valid"
                    DynamicTest.dynamicTest(displayName) { headerType.assertValid(ion) }
                }
                ion.hasTypeAnnotation("schema_footer") -> {
                    val displayName = "[$relativeLocation] schema_footer should be valid"
                    DynamicTest.dynamicTest(displayName) { footerType.assertValid(ion) }
                }
                isInvalidTypesTestCase(ion) && ion.islForIslCanValidate() -> {
                    val baseDescription = ion.getTextField("description")
                    val cases = (ion["invalid_types"] as IonList).mapIndexed { i, invalidType ->
                        val displayName = "[$relativeLocation] $baseDescription [$i]"
                        DynamicTest.dynamicTest(displayName) {
                            inlineTypeDefinition.assertInvalid(invalidType)
                        }
                    }
                    DynamicContainer.dynamicContainer("[$relativeLocation] $baseDescription", cases)
                }
                isInvalidSchemasTestCase(ion) && ion.islForIslCanValidate() -> {
                    val baseDescription = ion.getTextField("description")
                    val cases = (ion["invalid_schemas"] as IonList).mapIndexed { i, invalidSchema ->
                        val displayName = "[$relativeLocation] $baseDescription [$i]"
                        DynamicTest.dynamicTest(displayName) {
                            schemaType.assertInvalid(invalidSchema.asDocument())
                        }
                    }
                    DynamicContainer.dynamicContainer("[$relativeLocation] $baseDescription", cases)
                }
                isValidSchemasTestCase(ion) && ion.islForIslCanValidate() -> {
                    val baseDescription = ion.getTextField("description")
                    val cases = (ion["valid_schemas"] as IonList).mapIndexed { i, validSchema ->
                        val displayName = "[$relativeLocation] $baseDescription [$i]"
                        DynamicTest.dynamicTest(displayName) {
                            schemaType.assertValid(validSchema.asDocument())
                        }
                    }
                    DynamicContainer.dynamicContainer("[$relativeLocation] $baseDescription", cases)
                }
                else -> null
            }
        }
        return DynamicContainer.dynamicContainer(
            relativeLocation.path,
            f.toURI(),
            (dynamicNodeTestCases + validSchemaCase).stream()
        )
    }

    private fun IonStruct.islForIslCanValidate(): Boolean {
        return (this["isl_for_isl_can_validate"] as? IonBool)?.booleanValue() ?: true
    }
}
