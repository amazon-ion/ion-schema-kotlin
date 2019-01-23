package software.amazon.ionschema

import org.junit.Assert.*
import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.notification.RunNotifier
import org.junit.runner.RunWith
import org.junit.runner.notification.Failure
import org.junit.runners.Suite
import software.amazon.ion.*
import software.amazon.ion.system.IonSystemBuilder
import software.amazon.ion.system.IonTextWriterBuilder
import software.amazon.ionschema.internal.SchemaCore
import software.amazon.ionschema.internal.SchemaImpl
import software.amazon.ionschema.internal.TypeImpl
import software.amazon.ionschema.internal.util.*
import java.io.File
import java.io.FileReader
import java.io.OutputStream

@RunWith(IonSchemaTest::class)
@Suite.SuiteClasses(IonSchemaTest::class)
class IonSchemaTest(
        private val testClass: Class<Any>
    ) : Runner() {

    companion object {
        private val specialFieldNames = setOf("fields", "element")
    }

    private val ION = IonSystemBuilder.standard().build()
    private val schemaSystem = IonSchemaSystemBuilder.standard().build()
    private val schemaCore = SchemaCore(schemaSystem)

    override fun getDescription(): Description {
        return Description.createSuiteDescription(testClass)
    }

    override fun run(notifier: RunNotifier) {
        val base = "data/test"
        File(base).walk()
            .filter { it.isFile }
            .filter { it.path != "data/test/constraints/annotations/ordered.isl" }  // TBD remove
            .filter { it.path != "data/test/constraints/annotations/unordered.isl" }  // TBD remove
            .forEach { file ->
                val testName = file.path.substring(base.length + 1, file.path.length - ".isl".length)
                var schema: Schema? = null
                var type: Type? = null

                val iter = ION.iterate(FileReader(file)).asSequence().toList().listIterator()
                iter.forEach { ion ->
                    val annotation = ion.typeAnnotations[0]
                    when (annotation) {
                        "schema_header" -> {
                            iter.previous()
                            schema = SchemaImpl(schemaSystem, schemaCore, iter)
                        }

                        "type" ->
                            type = TypeImpl(ion as IonStruct, schemaCore)

                        "valid", "invalid" -> {
                            val expectValid = annotation == "valid"
                            (ion as IonContainer).forEach {
                                if (it.fieldName != null) {
                                    val testType = schema!!.getType(it.fieldName)
                                    testType ?: throw Exception("Unrecognized type name '${it.fieldName}'")
                                    (it as IonSequence).forEach {
                                        runTest(notifier, testName, it) {
                                            val violations = Validator.validate(testType, it)
                                            println(violations)
                                            assertEquals(expectValid, violations.isValid())
                                            assertEquals(expectValid, Validator.isValid(testType, it))
                                        }
                                    }
                                } else {
                                    if (type == null) {
                                        throw Exception("No type defined for test $testName")
                                    }
                                    runTest(notifier, testName, it) {
                                        val violations = Validator.validate(type!!, it)
                                        println(violations)
                                        assertEquals(expectValid, violations.isValid())
                                        assertEquals(expectValid, Validator.isValid(type!!, it))
                                    }
                                }
                            }
                        }

                        "invalid_schema" -> {
                            runTest(notifier, testName, ion) {
                                try {
                                    SchemaImpl(schemaSystem, schemaCore, (ion as IonSequence).iterator())
                                    fail("Expected an InvalidSchemaException")
                                } catch (e: InvalidSchemaException) {
                                }
                            }
                        }

                        "invalid_type" -> {
                            runTest(notifier, testName, ion) {
                                try {
                                    TypeImpl(ion as IonStruct, schemaCore)
                                    fail("Expected an InvalidSchemaException")
                                } catch (e: InvalidSchemaException) {
                                }
                            }
                        }

                        "test_validation" -> {
                            if (ion is IonStruct) {
                                val validationType =
                                    if (ion["type"] != null) {
                                        schema!!.getType(ion["type"] as IonSymbol)
                                    } else {
                                        type
                                    }

                                val value = ion.get("value")
                                val values = ion.get("values")
                                if (value == null && values == null) {
                                    throw Exception("Expected either 'value' or 'values' to be specified:  $ion")
                                }

                                val testValues = mutableListOf<IonValue>()
                                value?.let { testValues.add(it) }
                                values?.let { testValues.addAll(it as IonSequence) }

                                testValues.forEach {
                                    runTest(notifier, testName, it) {
                                        val violations = Validator.validate(validationType!!, it)
                                        if (!violations.isValid()) {
                                            println(violations)
                                            val writer = IonTextWriterBuilder.pretty().build(System.out as OutputStream)
                                            violations.toIon().writeTo(writer)
                                        }
                                        assertEquals(ion.get("violations"), violations.toIon())
                                        assertFalse(Validator.isValid(validationType!!, it))
                                    }
                                }
                            }
                        }

                        else -> throw Exception(
                                "Unrecognized annotation '$annotation' in ${file.path}")
                    }
                }
            }
    }

    private fun runTest(
            notifier: RunNotifier,
            testName: String,
            ion: IonValue,
            test: () -> Unit) {

        val desc = Description.createTestDescription(testName, ion.toString())
        try {
            notifier.fireTestStarted(desc)
            test()
        } catch (ae: AssertionError) {
            notifier.fireTestFailure(Failure(desc, ae))
        } catch (e: Exception) {
            notifier.fireTestFailure(Failure(desc, e))
        } finally {
            notifier.fireTestFinished(desc)
        }
    }

    private fun Violations.toIon(): IonList {
        val list = ION.newEmptyList()
        if (size > 0) {
            this.forEach {
                list.add(it.toIon())
            }
        }
        return list
    }

    private fun addNestedData(struct: IonStruct, violation: Violations) {
        if (violation.violations.size > 0) {
            val violationList = ION.newEmptyList()
            violation.violations.forEach {
                violationList.add(it.toIon())
            }
            struct.put("violations", violationList)
        }

        if (violation.children.size > 0) {
            val childList = ION.newEmptyList()
            violation.children.forEach {
                childList.add(it.toIon())
            }
            struct.put("children", childList)
        }
    }

    private fun Violation.toIon(): IonStruct {
        val struct = ION.newEmptyStruct()
        if (constraint != null) {
            val constr = constraint as IonValue
            if (constr is IonStruct
                    && !specialFieldNames.contains(constr.fieldName)) {
                struct.put("constraint", constr.clone())
            } else {
                val constraintStruct = ION.newEmptyStruct()
                constraintStruct.put(
                        constr.fieldName ?: "type",
                        constr.clone())
                struct.put("constraint", constraintStruct)
            }
        }
        if (code != null) {
            struct.put("code", ION.newSymbol(code))
        }
        addNestedData(struct, this)
        return struct
    }

    private fun ViolationChild.toIon(): IonStruct {
        val struct = ION.newEmptyStruct()
        if (path != null && !path.equals("")) {
            struct.put("path", ION.newString(path))
        }
        if (index != null) {
            struct.put("index", ION.newInt(index as Int))
        }
        if (value != null && value != null) {
            struct.put("value", (value as IonValue).clone())
        }
        addNestedData(struct, this)
        return struct
    }
}
