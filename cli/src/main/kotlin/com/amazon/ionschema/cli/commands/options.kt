package com.amazon.ionschema.cli.commands

import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ionschema.IonSchemaSystem
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.Schema
import com.amazon.ionschema.Type
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.groups.default
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file

/**
 * Returns a CliKt [OptionDelegate][com.github.ajalt.clikt.parameters.options.OptionDelegate] for a list of paths to use
 * as file-system Authorities.
 *
 * The delegated property has type `List<File>`.
 */
fun CliktCommand.authoritiesOption() = option(
    "-a", "--authority",
    help = "The root(s) of the file system authority(s). " +
        "Authorities are only required if you need to import a type from another " +
        "schema file or if you are loading a schema using the --id option."
)
    .file(canBeFile = false, mustExist = true, mustBeReadable = true)
    .multiple()

/**
 * Returns a CliKt [OptionDelegate][com.github.ajalt.clikt.parameters.options.OptionDelegate] for a [Boolean] that
 * indicates whether to use the Ion Schema Schemas authority.
 */
fun CliktCommand.useIonSchemaSchemaAuthorityOption() = option(
    "-I", "--isl-for-isl",
    help = "Indicates that the Ion Schema Schemas authority should be included in the schema system configuration."
).flag()

/**
 * Returns a CliKt [OptionDelegate][com.github.ajalt.clikt.parameters.options.OptionDelegate] for an ISL [Schema].
 *
 * The delegated property has type `IonSchemaSystem.() -> Schema`.
 */
fun CliktCommand.schemaOption() = mutuallyExclusiveOptions<IonSchemaSystem.() -> Schema>(

    option("--id", help = "The ID of a schema to load from one of the configured authorities.")
        .convert { { loadSchema(it) } },

    option("--schema-text", "-t", help = "The Ion text contents of a schema document.")
        .convert { { newSchema(it) } },

    option("--schema-file", "-f", help = "A schema file")
        .file(mustExist = true, mustBeReadable = true, canBeDir = false)
        .convert { { newSchema(it.readText()) } },

    option(
        "-v", "--version",
        help = "An empty schema document for the specified Ion Schema version. " +
            "The version must be specified as X.Y; e.g. 2.0"
    )
        .enum<IonSchemaVersion> { it.name.drop(1).replace("_", ".") }
        .convert { { newSchema(it.symbolText) } },

    name = "Schema",
    help = "All Ion Schema types are defined in the context of a schema document, so it is necessary to always " +
        "have a schema document, even if that schema document is an implicit, empty schema. If a schema is " +
        "not specified, the default is an implicit, empty Ion Schema 2.0 document."
).default { newSchema(IonSchemaVersion.v2_0.symbolText) }

/**
 * Returns a CliKt [ArgumentDelegate][com.github.ajalt.clikt.parameters.arguments.ArgumentDelegate] for an ISL type.
 *
 * The delegated property has type `Schema.() -> Type`.
 */
fun CliktCommand.typeArgument() = argument("TYPE", help = "An ISL type name or inline type definition.")
    .convert { createTypeLambdaForArgument(it) }

private fun createTypeLambdaForArgument(typeArg: String): Schema.() -> Type {
    return {
        when (val typeIon = getSchemaSystem().ionSystem.singleValue(typeArg)) {
            is IonSymbol -> getType(typeIon.stringValue()) ?: throw IllegalArgumentException("Type not found: $typeArg")
            is IonStruct -> newType(typeIon)
            else -> throw IllegalArgumentException("Not a valid type reference: $typeArg")
        }
    }
}
