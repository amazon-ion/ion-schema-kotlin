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

package com.amazon.ionschema.cli

import com.github.ajalt.clikt.core.PrintMessage
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.string.shouldMatch
import org.junit.Test

class VersionTest {

    @Test
    fun testVersionCommand() {
        shouldThrow<PrintMessage> {
            IonSchemaCli().parse(arrayOf("--version"))
        }.should {
            it.message shouldMatch """ion-schema-cli version \d+\.\d+\.\d+(-SNAPSHOT)?-[0-9a-f]{7}"""
        }
    }
}
