/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

/**
 * A callback function for logging by the IonSchemaSystem.
 *
 * This allows the IonSchemaSystem implementation to be agnostic to
 * the actual logging framework used in an application.
 */
typealias IonSchemaSystemLogger = (level: LogLevel, message: () -> String) -> Unit

enum class LogLevel {
    /**
     * Used for logging general information about the execution of library routines.
     */
    Info,

    /**
     * Used for logging information about potentially harmful situations and
     * things that may be deprecated in future versions of the Ion Schema Language
     * or the `ion-schema-kotlin` library.
     *
     * [Warn] is the most severe level of logging, since the IonSchemaSystem will
     * throw a detailed exception for any unrecoverable errors.
     */
    Warn
}
