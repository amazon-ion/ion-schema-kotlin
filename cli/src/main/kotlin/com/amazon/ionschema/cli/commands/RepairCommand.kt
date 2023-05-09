package com.amazon.ionschema.cli.commands

import com.amazon.ionschema.cli.commands.repair.FixTransitiveImportsCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands

class RepairCommand : NoOpCliktCommand(
    help = "Fixes schemas that are affected by a bug in some way."
) {
    init {
        context {
            subcommands(
                FixTransitiveImportsCommand()
            )
        }
    }
}
