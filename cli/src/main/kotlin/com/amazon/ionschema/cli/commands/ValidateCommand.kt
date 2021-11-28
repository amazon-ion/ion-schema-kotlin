package com.amazon.ionschema.cli.commands

import com.amazon.ion.IonStruct
import com.amazon.ion.IonText
import com.amazon.ion.IonType
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.AuthorityFilesystem
import com.amazon.ionschema.IonSchemaSystemBuilder
import com.amazon.ionschema.ResourceAuthority
import com.amazon.ionschema.Schema
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.defaultByName
import com.github.ajalt.clikt.parameters.groups.groupChoice
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file

class ValidateCommand : CliktCommand(
    help = "Validate Ion data against a schema.",
    epilog = """
        ```
        Example usage:
            Validate against a new type:
                ion-schema-cli validate --type '{ codepoint_length: range::[min, 10] }' 'hello'
            Validate against a type from a schema:
                ion-schema-cli validate -a file-system --base-dir ~/my_schemas/ --schema 'Customers.isl' --type 'online_customer' '{ foo: bar }'    
        ```
    """.trimIndent()
) {

    private val ion = IonSystemBuilder.standard().build()

    sealed class AuthorityConfig : OptionGroup() {
        class FileSystem : AuthorityConfig() {
            val baseDir by option(help = "The root(s) of the file system authority(s)")
                .file(canBeFile = false, mustExist = true, mustBeReadable = true)
                .multiple()
        }
        class IonSchemaSchemas : AuthorityConfig()
        object None : AuthorityConfig()
    }

    val authorityConfig by option("-a", "--authority").groupChoice(
        "file-system" to AuthorityConfig.FileSystem(),
        "isl" to AuthorityConfig.IonSchemaSchemas(),
        "none" to AuthorityConfig.None
    ).defaultByName("none")

    val iss by lazy {
        val authorities = when (authorityConfig) {
            is AuthorityConfig.FileSystem -> (authorityConfig as AuthorityConfig.FileSystem).baseDir
                .map { AuthorityFilesystem(it.absolutePath) }
            is AuthorityConfig.IonSchemaSchemas -> listOf(ResourceAuthority.forIonSchemaSchemas())
            is AuthorityConfig.None -> emptyList()
        }

        IonSchemaSystemBuilder.standard()
            .withIonSystem(ion)
            .withAuthorities(authorities)
            .allowTransitiveImports(false)
            .build()
    }

    private val schema by mutuallyExclusiveOptions<() -> Schema>(
        option("--id", help = "schema id to load")
            .convert { { iss.loadSchema(it) } },
        option("--schema-text", "-st", help = "schema text")
            .convert { { iss.newSchema(it) } },
        option("--schema-file", "-sf", help = "schema file")
            .file(mustExist = true, mustBeReadable = true, canBeDir = false)
            .convert { { iss.newSchema(it.readText()) } },
        name = "Schema",
        help = "Optional schema to load. If not specified, an empty ISL 1.0 schema is used."
    )

    private val type by option("-t", "--type", help = "An ISL type reference.").required()
        .check(lazyMessage = { "Not a valid type reference: $it" }) {
            with(ion.singleValue(it)) {
                !isNullValue && type in listOf(IonType.SYMBOL, IonType.STRUCT, IonType.STRING)
            }
        }

    private val isDocument by option("-d", "--document", help = "Indicates that ION_DATA should be read as an Ion document rather than as a single value.").flag()

    private val ionData by argument("ION_DATA")
        .convert {
            val result = runCatching {
                if (isDocument) {
                    ion.loader.load(it)
                } else {
                    ion.singleValue(it)
                }
            }
            require(result.isSuccess) { "Unable to parse ION_DATA: $it ; ${result.exceptionOrNull()}" }
            result.getOrThrow()
        }

    override fun run() {
        val islSchema = schema?.invoke() ?: iss.newSchema()

        val typeIon = iss.ionSystem.singleValue(type)
        val islType = if (typeIon is IonText) {
            islSchema.getType(typeIon.stringValue()) ?: throw IllegalArgumentException("No such type: $type -- ${islSchema.getTypes().asSequence().map { it.name }.toList()}")
        } else {
            islSchema.newType(typeIon as IonStruct)
        }

        val violations = islType.validate(ionData)

        if (violations.isValid()) {
            echo("Valid")
        } else {
            echo(violations.toString().dropWhile { it != '\n' }.trim())
        }
    }
}
