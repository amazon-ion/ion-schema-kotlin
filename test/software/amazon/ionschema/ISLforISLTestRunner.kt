package software.amazon.ionschema

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.runner.notification.RunNotifier
import org.junit.runner.RunWith
import org.junit.runners.Suite
import java.io.File
import java.io.FileReader

/**
 * This test runner uses the ISL for ISL itself to validate the types and schemas
 * defined in the file-based test suite.
 */
@RunWith(ISLforISLTestRunner::class)
@Suite.SuiteClasses(ISLforISLTestRunner::class)
class ISLforISLTestRunner(
        testClass: Class<Any>
) : AbstractTestRunner(testClass) {

    private val schemaSystem = IonSchemaSystemBuilder.standard()
            .withAuthority(AuthorityFilesystem("schema"))
            .build()

    private val blacklist = setOf(
            "data/test/schema/import/import_type_unknown.isl",
            "data/test/schema/import/invalid_duplicate_import.isl",
            "data/test/schema/import/invalid_duplicate_import_type.isl",
            "data/test/schema/import/invalid_duplicate_type.isl",
            "data/test/schema/invalid_missing_schema_footer.isl",
            "data/test/schema/invalid_missing_schema_header.isl",
            "data/test/schema/invalid_unknown_type.isl"
    )

    override fun run(notifier: RunNotifier) {
        val islSchema = schemaSystem.loadSchema("isl/schema.isl")
        val schema = islSchema.getType("schema")!!
        val type = islSchema.getType("type")!!

        val base = "data/test"
        File(base).walk()
            .filter { it.isFile }
            .filter { it.path.endsWith(".isl") }
            .filter { !blacklist.contains(it.path) }
            .forEach { file ->
                val testName = file.path.substring(base.length + 1, file.path.length - ".isl".length)

                val iter = ION.iterate(FileReader(file)).asSequence().toList().listIterator()
                iter.forEach { ion ->
                    val annotation = ion.typeAnnotations[0]
                    when (annotation) {
                        "schema_header" -> {
                            iter.previous()
                            val datagram = ION.newDatagram()
                            do {
                                val v = iter.next()
                                datagram.add(v)
                            } while (iter.hasNext() && !v.hasTypeAnnotation("schema_footer"))

                            runTest(notifier, testName, datagram) {
                                println(datagram)
                                val result = schema.validate(datagram)
                                println(result)
                                assertTrue(result.isValid())
                            }
                        }

                        "invalid_schema" ->
                            runTest(notifier, testName, ion) {
                                val value = prepareValue(ion)
                                println(value)
                                val result = schema.validate(value)
                                println(result)
                                assertFalse(result.isValid())
                            }

                        "type" ->
                            runTest(notifier, testName, ion) {
                                println(ion)
                                val result = type.validate(ion)
                                println(result)
                                assertTrue(result.isValid())
                            }

                        "invalid_type" ->
                            runTest(notifier, testName, ion) {
                                println(ion)
                                val result = type.validate(ion)
                                println(result)
                                assertFalse(result.isValid())
                            }
                    }
                }
            }
    }
}

