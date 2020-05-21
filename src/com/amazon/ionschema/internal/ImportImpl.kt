package com.amazon.ionschema.internal

import com.amazon.ionschema.Import
import com.amazon.ionschema.Type

/**
 * Implementation of [Import] for all user-provided ISL.
 */
internal class ImportImpl(
        override val id: String,
        private val types: Map<String, Type>
) : Import {

    override fun getType(name: String) = types[name]

    override fun getTypes() = types.values.iterator()
}

