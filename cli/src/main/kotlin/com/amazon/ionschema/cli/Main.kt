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

import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.parameters.options.versionOption
import java.util.Properties

fun main(args: Array<String>) = IonSchemaCli().main(args)

/**
 * This is the root command. It doesn't run anything on its own—you must invoke a subcommand.
 */
class IonSchemaCli : NoOpCliktCommand(
    name = "ion-schema-cli",
    help = "TODO"
) {
    init {
        context {
            subcommands(
                // TODO: Add subcommands
            )
            versionOption(getVersionString())
            helpFormatter = CliktHelpFormatter(showRequiredTag = true, showDefaultValues = true)
        }
    }

    private fun getVersionString(): String {
        val propertiesStream = this.javaClass.getResourceAsStream("/cli.properties")
        val properties = Properties().apply { load(propertiesStream) }
        return "${properties.getProperty("version")}-${properties.getProperty("commit")}"
    }
}
