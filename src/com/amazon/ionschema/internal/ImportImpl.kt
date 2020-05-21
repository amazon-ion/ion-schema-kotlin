package com.amazon.ionschema.internal

import com.amazon.ionschema.Import
import com.amazon.ionschema.Schema
import com.amazon.ionschema.Type

/**
 * Implementation of [Import] for all user-provided ISL.
 */
internal class ImportImpl(
        override val id: String,
        private val schema: Schema?,
        private val types: Map<String, Type> = emptyMap()
) : Import {

    override fun getType(name: String) = schema?.getType(name) ?: types[name]

    override fun getTypes() =
            ((schema?.getTypes()?.asSequence() ?: emptySequence())
                    + types.values.asSequence()).iterator()
}

