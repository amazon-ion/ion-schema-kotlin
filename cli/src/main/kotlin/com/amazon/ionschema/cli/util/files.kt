package com.amazon.ionschema.cli.util

import java.io.File

/**
 * Does some action for each `.isl` file that is in the given path
 */
fun forEachSchemaInPath(path: String, action: (schemaId: String, schemaFile: File) -> Unit): List<Pair<String, Throwable>> {
    return File(path).walk()
        .filter { it.isFile }
        .filter { it.path.endsWith(".isl") }
        .map { file -> file.path.substring(path.length + 1) to file }
        .mapNotNull { (schemaId, file) ->
            runCatching { action.invoke(schemaId, file) }
                .exceptionOrNull()
                ?.also { it.printStackTrace() }
                ?.let { schemaId to it }
        }.toList()
}

/**
 * Copies a file from the current [basePath] to the [newBasePath], modifying the contents according to the given [patchSet].
 */
fun rewriteFile(file: File, basePath: String, newBasePath: String, patchSet: PatchSet) {
    val schemaId = file.path.substring(basePath.length + 1)
    val newFile = File("$newBasePath/$schemaId")
    newFile.parentFile.mkdirs()
    if (patchSet.hasChanges()) {
        newFile.createNewFile()
        val schemaIonText = file.readText(Charsets.UTF_8)
        newFile.appendText(patchSet.applyTo(schemaIonText))
    } else {
        file.copyTo(newFile)
    }
}

/**
 * Attempts to detect the indentation of a multiline String.
 */
fun String.inferIndent(): String? {
    fun String.indentWidth(): Int = indexOfFirst { !it.isWhitespace() }.let { if (it == -1) length else it }

    val lineIndentation = lines()
        .filter { it.isNotBlank() }
        .mapNotNull {
            if (it.startsWith(" ")) {
                it.indentWidth()
            } else if (it.startsWith("\t")) {
                // Found a tab? Assume all indentation is with tabs.
                return "\t"
            } else {
                null // No indentation on this line.
            }
        }

    // If we're returning here, it's because there are no indented lines
    val min = lineIndentation.minOfOrNull { it } ?: return null

    return if (lineIndentation.all { it % min == 0 }) {
        " ".repeat(min)
    } else {
        // Indentation is inconsistent -- cannot be determined
        null
    }
}
