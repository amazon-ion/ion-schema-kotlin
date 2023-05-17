package com.amazon.ionschema.cli.util

import java.util.TreeSet

class PatchSet {
    private data class Patch(val start: Int, val endInclusive: Int, val replacementText: String)

    private val patchSet = TreeSet<Patch>(compareByDescending { it.start })

    fun hasChanges() = patchSet.isNotEmpty()

    fun patch(start: Int, endInclusive: Int, replacementText: String) {
        patchSet.add(Patch(start, endInclusive, replacementText))
    }

    fun replaceAll(newText: String) {
        patchSet.add(Patch(0, -1, newText))
    }

    fun applyTo(original: String): String {
        val sb = StringBuilder(original)
        for ((start, endInclusive, newText) in patchSet) {
            val endExclusive = if (endInclusive == -1) original.length else endInclusive + 1
            sb.replace(start, endExclusive, newText)
        }
        return sb.toString()
    }
}
