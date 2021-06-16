/*
 * Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import com.amazon.ion.IonString
import com.amazon.ion.IonValue
import com.amazon.ion.system.IonSystemBuilder
import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier

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
        test: () -> Unit
    ) {

        val desc = Description.createTestDescription(testName, ion.toString())
        try {
            notifier.fireTestStarted(desc)
            test()
        } catch (ae: AssertionError) {
            notifier.fireTestFailure(Failure(desc, ae))
        } catch (e: Throwable) {
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
