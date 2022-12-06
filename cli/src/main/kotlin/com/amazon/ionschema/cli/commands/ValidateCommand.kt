package com.amazon.ionschema.cli.commands

import com.amazon.ion.IonList
import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonType
import com.amazon.ion.IonValue
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.AuthorityFilesystem
import com.amazon.ionschema.IonSchemaSystem
import com.amazon.ionschema.IonSchemaSystemBuilder
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.ResourceAuthority
import com.amazon.ionschema.Schema
import com.amazon.ionschema.Violations
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.check
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.groups.default
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import kotlin.system.exitProcess

class ValidateCommand : CliktCommand(
    help = "Validate Ion data for a given Ion Schema type.",
    epilog = """
        ```
        Example usage:
        
            Validate values for a type:
                ion-schema-cli validate '{ codepoint_length: 5, utf8_byte_length: range::[4,6] }' hello hello_world 1 '{:(:}'
            Output:
                valid::[]
                invalid::[{constraint:codepoint_length,code:invalid_codepoint_length,message:"invalid codepoint length 11, expected range::[5,5]"},{constraint:utf8_byte_length,code:invalid_utf8_byte_length,message:"invalid utf8 byte length 11, expected range::[4,6]"}]
                invalid::[{constraint:codepoint_length,code:invalid_type,message:"not applicable for type int"},{constraint:utf8_byte_length,code:invalid_type,message:"not applicable for type int"}]
                error::{type:"IonReaderTextParsingException",message:"unable to parse Ion: Syntax error at line 1 offset 3: invalid syntax [state:STATE_BEFORE_FIELD_NAME on token:TOKEN_COLON]"}
        
            Validate data for an inline-defined type:
                ion-schema-cli validate '{ codepoint_length: range::[min, 10] }' 'hello'
                
            Validate data for a type from a schema that is loaded from an authority:
                ion-schema-cli validate --authority ~/my_schemas/ --id 'Customers.isl' --type 'online_customer' '{ foo: bar }'
            
            Read multiple lines and validate as a single document:
                echo -e "hello \n world" | ./ion-schema-cli validate -ds  document
            
            Validate each line as a separate document:
                echo -e "hello \n world" | ./ion-schema-cli validate -d  document
                
            Validate each line as a single value:
                echo -e "hello \n world" | ./ion-schema-cli validate symbol

            Validate an ion file as a document:
                ion-schema-cli validate -ds -f ~/my_schemas/my_document_schema.isl my_specialized_document < my_data.ion
            
            Validate in REPL mode (use control-d to send EOF character and exit cleanly):
                ion-schema-cli validate '{ codepoint_length: 5 }'
        ```
    """.trimIndent()
) {

    private val ion = IonSystemBuilder.standard().build()

    private val fileSystemAuthorities by option(
        "-a", "--authority",
        help = "The root(s) of the file system authority(s). " +
            "Authorities are only required if you need to import a type from another " +
            "schema file or if you are loading a schema using the --id option."
    )
        .file(canBeFile = false, mustExist = true, mustBeReadable = true)
        .multiple()

    private val useIonSchemaSchemaAuthority by option(
        "-I", "--isl-for-isl",
        help = "Indicates that the Ion Schema Schemas authority should be included in the schema system configuration."
    ).flag()

    val iss by lazy {
        IonSchemaSystemBuilder.standard()
            .apply {
                withIonSystem(ion)
                withAuthorities(fileSystemAuthorities.map { AuthorityFilesystem(it.path) })
                if (useIonSchemaSchemaAuthority) addAuthority(ResourceAuthority.forIonSchemaSchemas())
                allowTransitiveImports(false)
            }
            .build()
    }

    private val schema by mutuallyExclusiveOptions<IonSchemaSystem.() -> Schema>(

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

    private val type by argument(help = "An ISL type name or inline type definition.")
        .check(lazyMessage = { "Not a valid type reference: $it" }) {
            with(ion.singleValue(it)) {
                !isNullValue && type in listOf(IonType.SYMBOL, IonType.STRUCT)
            }
        }

    private val isDocument by option(
        "-d", "--document",
        help = "Indicates that IONDATA should be interpreted as an Ion document rather than as individual value(s)."
    ).flag(default = false)

    private val slurpLines by option(
        "-s", "--slurp",
        help = "When reading from STDIN, indicates that lines should be slurped together before parsing the Ion."
    ).flag(default = false)

    private val isNonZeroExitCodeOnInvalidData by option(
        "-F", "--fail-on-invalid",
        help = "Sets the command to return a non-zero exit code when a value is invalid for the given type."
    ).flag(default = false)

    private val isQuiet by option(
        "-q", "--quiet",
        help = "Suppress the validation results messages"
    ).flag(default = false)

    private val ionData by argument(help = "Ion data to validate. If no value(s) are provided, this tool will attempt to read values from STDIN.")
        .multiple()

    override fun run() {
        val islSchema = iss.schema()

        val typeIon = iss.ionSystem.singleValue(type)
        val islType = if (typeIon is IonSymbol) {
            islSchema.getType(typeIon.stringValue()) ?: throw IllegalArgumentException("Type not found: $type")
        } else {
            islSchema.newType(typeIon as IonStruct)
        }

        val data = if (ionData.isEmpty()) readFromStdIn() else ionData.asSequence()

        var numInvalid = 0
        var numErr = 0

        data.forEach {
            val result = parseIon(it)
                .map { value -> islType.validate(value).toIon() }
                .getOrElse { convertErrorToIon(it) }

            if (result.hasTypeAnnotation("error")) {
                numErr++
                echo(result, err = isQuiet)
            } else {
                if (result.hasTypeAnnotation("invalid")) numInvalid++
                if (!isQuiet) echo(result)
            }
        }

        if (numErr > 0) exitProcess(2)
        if (numInvalid > 0 && isNonZeroExitCodeOnInvalidData) exitProcess(1)
    }

    private fun readFromStdIn() = generateSequence { readlnOrNull() }
        .let { lines ->
            if (slurpLines) {
                sequenceOf(lines.joinToString(" "))
            } else {
                lines
            }
        }

    private fun parseIon(ionText: String): Result<IonValue> {
        return if (isDocument) {
            runCatching { ion.loader.load(ionText) }
        } else {
            runCatching { ion.singleValue(ionText) }
        }
    }

    private fun convertErrorToIon(t: Throwable): IonValue {
        return ion.newEmptyStruct().apply {
            setTypeAnnotations("error")
            add("type").newString(t.javaClass.simpleName)
            add("message").newString("unable to parse Ion: ${t.message}")
        }
    }

    private fun Violations.toIon(): IonValue {
        val result = this.toIonCauseList() ?: ion.newEmptyList()
        if (violations.size > 0 || children.size > 0) {
            result.setTypeAnnotations("invalid")
        } else {
            result.setTypeAnnotations("valid")
        }
        return result.apply { makeReadOnly() }
    }

    private fun Violations.toIonCauseList(): IonList? {
        if (violations.isEmpty() && children.isEmpty()) return null
        val result = this
        return ion.newEmptyList().apply {
            result.violations.forEach { v ->
                add().newEmptyStruct().apply {
                    v.constraint?.fieldName?.let { add("constraint").newSymbol(it) }
                    add("code").newSymbol(v.code)
                    add("message").newString(v.message)
                    v.toIonCauseList()?.let { add("caused_by", it) }
                }
            }
            result.children.forEach { child ->
                add().newEmptyStruct().apply {
                    child.fieldName?.let { add("field_name").newSymbol(it) }
                    child.index?.let { add("index").newInt(it) }
                    child.value?.let { add("value_prefix").newString(it.toString().truncate(20)) }
                    child.toIonCauseList()?.let { add("caused_by", it) }
                }
            }
        }
    }

    private fun String.truncate(limit: Int, truncated: CharSequence = "..."): String {
        return if (length < limit) this else substring(0, limit) + truncated
    }
}
