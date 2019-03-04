package software.amazon.ionschema

import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import software.amazon.ion.IonString
import software.amazon.ion.IonValue
import software.amazon.ion.system.IonSystemBuilder

abstract class AbstractTestRunner(
        private val testClass: Class<Any>
) : Runner() {

    internal val ION = IonSystemBuilder.standard().build()

    override fun getDescription(): Description {
        return Description.createSuiteDescription(testClass)
    }

    internal fun runTest(
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

    internal fun prepareValue(ion: IonValue) =
        if (ion.hasTypeAnnotation("document") && ion is IonString) {
            ION.loader.load(ion.stringValue())
        } else {
            ion
        }
}

