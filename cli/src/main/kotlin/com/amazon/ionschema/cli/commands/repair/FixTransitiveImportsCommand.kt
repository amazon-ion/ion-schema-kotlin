package com.amazon.ionschema.cli.commands.repair

import com.amazon.ion.IonSystem
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.Authority
import com.amazon.ionschema.AuthorityFilesystem
import com.amazon.ionschema.IonSchemaSchemas
import com.amazon.ionschema.cli.commands.authoritiesOption
import com.amazon.ionschema.cli.commands.useIonSchemaSchemaAuthorityOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit

class FixTransitiveImportsCommand : CliktCommand(
    help = "Fixes schemas that are affected by the transitive import issue. See https://github.com/amzn/ion-schema/issues/39",
    epilog = """
        ```
        Example usage:   
            ion-schema-cli repair fix-transitive-imports cli/src/test/resources/FixTransitiveImportsCommand/input/
        ```
    """.trimIndent()
) {

    private val ion: IonSystem = IonSystemBuilder.standard().build()
    private val fileSystemAuthorityRoots by authoritiesOption()
    private val useIonSchemaSchemaAuthority by useIonSchemaSchemaAuthorityOption()

    private val pathToFix by argument(
        "BASE_DIRECTORY",
        help = """
        The path in which to fix schema files.
        This path is implicitly used as an AuthorityFilesystem, so there is no need to also pass this path with the '-a' option.
        """.trimIndent()
    )
        .file(mustExist = true, mustBeReadable = true, canBeFile = false).convert { it.path }

    private val destination by option(
        "-d", "--destination", metavar = "PATH",
        help = """
        A path for the new files to be written to.
        This must be a writeable directory.
        """.trimIndent()
    ).file(canBeFile = false)
        .convert<File, () -> String> { it::getPath }
        .default(
            { pathToFix + "_" + Instant.now().truncatedTo(ChronoUnit.SECONDS) },
            defaultForHelp = "'<BASE_DIRECTORY>_<UTC_TIMESTAMP>'"
        )

    private val skipCleanup by option(help = "Skips cleanup of temp files—potentially useful for debugging problems.")
        .flag(default = false)

    private val verbose by option("-v", "--verbose").flag()

    private val strategy by option(
        "-s", "--strategy",
        help = """
        KeepWildcards -- While fixing imports, keeps all existing "wildcard" imports (i.e. `{id:<SCHEMA_ID>}`),
        but all newly added imports will be type imports (i.e. `{id:<SCHEMA_ID>,type:<TYPE_NAME>}`).
        This strategy will introduce smaller changes and will not introduce any naming conflicts.
        However, the presence of wildcard imports means that unintentional name conflicts could happen in the future.
        
        NoWildcards -- While fixing imports, rewrite all imports as `{id:<SCHEMA_ID>,type:<TYPE_NAME>}`.
        This may generate a larger diff, but it is guaranteed to never have name conflicts unintentionally 
        introduced by a dependency in the future.
        
        PreferWildcards -- This strategy will cause the rewriter to prefer wildcard imports in the schema header.
        This strategy may introduce name conflicts that result in an invalid schema. If that occurs, try again
        using a different strategy.
        """.trimIndent()
    )
        .enum<TransitiveImportRewriter.ImportStrategy>()
        .default(TransitiveImportRewriter.ImportStrategy.KeepWildcards)

    override fun run() {
        val authorities = mutableListOf<Authority>()
        if (useIonSchemaSchemaAuthority) authorities += IonSchemaSchemas.authority()
        fileSystemAuthorityRoots.filter { it.path != pathToFix }
            .mapTo(authorities) { AuthorityFilesystem(it.path) }

        val destination = destination()
        File(destination).let {
            if (!it.exists()) it.mkdirs()
            require(it.canWrite()) { "Cannot write to ${it.canonicalPath}" }
        }

        val rewriter = TransitiveImportRewriter(strategy, ion, skipCleanup, echo = if (verbose) ({ echo(it) }) else ({}))
        rewriter.fixTransitiveImports(pathToFix, destination, authorities)
    }
}
