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

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.io.File
import java.util.stream.Stream
import kotlin.streams.asStream

/**
 * Annotation that can be used to provide ISL test files for a [ParameterizedTest].
 *
 * You can also call [IonSchemaTestFilesSource.asSequence] to get a [Sequence] of [File].
 */
@ArgumentsSource(IonSchemaTestFilesSource.Provider::class)
annotation class IonSchemaTestFilesSource(val except: Array<String> = []) {
    companion object {
        const val ION_SCHEMA_TESTS_DIR = "../ion-schema-tests"

        @JvmStatic
        fun asSequence(excluding: List<String> = emptyList()): Sequence<File> {
            return File(ION_SCHEMA_TESTS_DIR).walk()
                .filter { it.isFile }
                .filter { it.path.endsWith(".isl") }
                .filter { it.path !in excluding }
        }
    }

    class Provider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
            val annotation = context.element.get().getAnnotation(IonSchemaTestFilesSource::class.java)!!
            val excludeList = annotation.except.toList()
            return asSequence(excluding = excludeList)
                .map { Arguments { arrayOf(it) } }
                .asStream()
        }
    }
}
