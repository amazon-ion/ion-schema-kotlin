package software.amazon.ionschema

import org.junit.Assert.*
import org.junit.runner.notification.RunNotifier
import org.junit.runner.RunWith
import org.junit.runners.Suite
import software.amazon.ion.IonContainer
import software.amazon.ion.IonList
import software.amazon.ion.IonSequence
import software.amazon.ion.IonStruct
import software.amazon.ion.IonSymbol
import software.amazon.ion.IonValue
import software.amazon.ion.system.IonTextWriterBuilder
import software.amazon.ionschema.internal.IonSchemaSystemImpl
import software.amazon.ionschema.internal.SchemaCore
import software.amazon.ionschema.internal.SchemaImpl
import software.amazon.ionschema.internal.TypeImpl
import java.io.File
import java.io.FileReader
import java.io.OutputStream

/**
 * Primary test runner for the file-based test suite.
 */
@RunWith(IonSchemaTestRunner::class)
@Suite.SuiteClasses(IonSchemaTestRunner::class)
class IonSchemaTestRunner(
        testClass: Class<Any>
) : AbstractTestRunner(testClass) {

    private val schemaSystem = IonSchemaSystemBuilder.standard()
            .withAuthority(AuthorityFilesystem("data/test"))
            .build()

    private val schemaCore = SchemaCore(schemaSystem)

    private val blacklist = setOf(
            "data/test/constraints/not/empty_type.isl"
    )

    private val specialFieldNames = setOf("fields", "element")

    override fun run(notifier: RunNotifier) {
        val base = "data/test"
        File(base).walk()
            .filter { it.isFile }
            .filter { it.path.endsWith(".isl") }
            .filter { !blacklist.contains(it.path) }
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
                            schema = SchemaImpl(schemaSystem as IonSchemaSystemImpl, schemaCore, iter)
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
                                        val value = prepareValue(it)
                                        runTest(notifier, testName, value) {
                                            val violations = testType.validate(value)
                                            println(violations)
                                            assertEquals(expectValid, violations.isValid())
                                            assertEquals(expectValid, testType.isValid(value))
                                        }
                                    }
                                } else {
                                    if (type == null) {
                                        throw Exception("No type defined for test $testName")
                                    }
                                    val value = prepareValue(it)
                                    runTest(notifier, testName, value) {
                                        val violations = type!!.validate(value)
                                        println(violations)
                                        assertEquals(expectValid, violations.isValid())
                                        assertEquals(expectValid, type!!.isValid(value))
                                    }
                                }
                            }
                        }

                        "invalid_schema" -> {
                            runTest(notifier, testName, ion) {
                                try {
                                    SchemaImpl(schemaSystem as IonSchemaSystemImpl, schemaCore,
                                            (prepareValue(ion) as IonSequence).iterator())
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
                                        schema!!.getType((ion["type"] as IonSymbol).stringValue())
                                    } else {
                                        type
                                    }

                                val theValue = ion.get("value")
                                val theValues = ion.get("values")
                                if (theValue == null && theValues == null) {
                                    throw Exception("Expected either 'value' or 'values' to be specified:  $ion")
                                }

                                val testValues = mutableListOf<IonValue>()
                                theValue?.let { testValues.add(it) }
                                theValues?.let { testValues.addAll(it as IonSequence) }

                                testValues.forEach {
                                    val value = prepareValue(it)
                                    runTest(notifier, testName, value) {
                                        val violations = validationType!!.validate(value)
                                        if (!violations.isValid()) {
                                            println(violations)
                                            val writer = IonTextWriterBuilder.pretty().build(System.out as OutputStream)
                                            violations.toIon().writeTo(writer)
                                        }
                                        assertEquals(ion.get("violations"), violations.toIon())
                                        assertFalse(validationType.isValid(value))
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

    private fun Violations.toIon(): IonList {
        val list = ION.newEmptyList()
        this.forEach {
            list.add(it.toIon())
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
        if (fieldName != null && !fieldName.equals("")) {
            struct.put("fieldName", ION.newString(fieldName))
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

