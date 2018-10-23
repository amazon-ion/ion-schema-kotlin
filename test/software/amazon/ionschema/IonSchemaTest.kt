package software.amazon.ionschema

import org.junit.Assert.*
import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.notification.RunNotifier
import org.junit.runner.RunWith
import org.junit.runner.notification.Failure
import org.junit.runners.Suite
import software.amazon.ion.*
import software.amazon.ionschema.internal.ION
import software.amazon.ionschema.internal.SchemaCore
import software.amazon.ionschema.internal.SchemaImpl
import software.amazon.ionschema.internal.TypeImpl
import java.io.File
import java.io.FileReader

@RunWith(IonSchemaTest::class)
@Suite.SuiteClasses(IonSchemaTest::class)
class IonSchemaTest(
        private val testClass: Class<Any>
    ) : Runner() {

    private val schemaSystem = IonSchemaSystem.Builder.standard().build()
    private val schemaCore = SchemaCore(schemaSystem)

    override fun getDescription(): Description {
        return Description.createSuiteDescription(testClass)
    }

    override fun run(notifier: RunNotifier) {
        val base = "data/test"
        File(base).walk()
            .filter { it.isFile }
            .filter { !it.path.contains("constraints/annotations") }  // TBD remove
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
                                    if (testType == null) {
                                        throw Exception("Unrecognized type name '${it.fieldName}'")
                                    }
                                    (it as IonSequence).forEach {
                                        runTest(notifier, testName, it) {
                                            assertEquals(expectValid, testType.isValid(it))
                                        }
                                    }
                                } else {
                                    if (type == null) {
                                        throw Exception("No type defined for test $testName")
                                    }
                                    runTest(notifier, testName, it) {
                                        assertEquals(expectValid, type!!.isValid(it))
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
}
