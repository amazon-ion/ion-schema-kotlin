/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import com.amazon.ion.IonDatagram
import com.amazon.ion.IonSexp
import com.amazon.ion.IonString
import com.amazon.ion.IonStruct
import com.amazon.ion.IonText
import com.amazon.ion.IonValue
import com.amazon.ion.IonWriter
import com.amazon.ion.system.IonSystemBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

internal val ION = IonSystemBuilder.standard().build()

/**
 * Returns the test directory name for an IonSchemaVersion.
 */
internal val IonSchemaVersion.testSuiteDirectoryName: String
    get() = symbolText.drop(1)

internal fun prepareValue(ion: IonValue) =
    if (ion.hasTypeAnnotation("document") && ion is IonString) {
        ION.loader.load(ion.stringValue())
    } else {
        ion
    }

internal fun IonValue.asDocument(): IonDatagram {
    require(this is IonSexp) { "Malformed test; expected a sexp, found a $type." }
    return ION.newDatagram().apply {
        addAll(this@asDocument.map { it.clone() })
        makeReadOnly()
    }
}

internal fun maybeConvertToDocument(ion: IonValue) =
    if (ion.hasTypeAnnotation("document") && ion is IonSexp) {
        ION.newDatagram().apply {
            addAll(ion.map { it.clone() })
        }
    } else {
        ion
    }

fun Type.assertValid(value: IonValue) {
    val violations = this.validate(value)
    println(violations)
    assertTrue(violations.isValid()) { "expected valid $name, but was invalid: $value" }
    assertTrue(this.isValid(value))
}

fun Type.assertInvalid(value: IonValue) {
    val violations = this.validate(value)
    println(violations)
    assertFalse(violations.isValid()) { "expected invalid $name, but was valid: $value" }
    assertFalse(this.isValid(value))
}

fun Type.assertValidity(expectIsValid: Boolean, value: IonValue) {
    if (expectIsValid) assertValid(value) else assertInvalid(value)
}

internal fun IonStruct.getTextField(fieldName: String) = (get(fieldName) as IonText).stringValue()

/**
 * Asserts that the values written to an [IonWriter] match the expected Ion.
 */
fun assertEqualIon(expected: String, block: (IonWriter) -> Unit) = assertEqualIon(ION.loader.load(expected), block)

/**
 * Asserts that the values written to an [IonWriter] match the expected Ion.
 */
fun assertEqualIon(expected: IonDatagram, block: (IonWriter) -> Unit) {
    val newDg = ION.newDatagram()
    val ionWriter = ION.newWriter(newDg)
    ionWriter.apply(block)
    Assertions.assertEquals(expected, newDg)
}
