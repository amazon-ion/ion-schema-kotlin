package com.amazon.ionschema.reader.internal

import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.TypeArgument
import com.amazon.ionschema.model.UserReservedFields

/**
 * Stores context that is used while reading Ion values as Ion Schema.
 */
@OptIn(ExperimentalIonSchemaModel::class)
internal class ReaderContext(
    /**
     * The [UserReservedFields] of the current schema. Initially, this is an empty [UserReservedFields], but it can be
     * mutated if a reader implementation encounters a schema header.
     */
    var userReservedFields: UserReservedFields = UserReservedFields(),
    /**
     * A list of [TypeArgument]s that are either a type name or an inline import that need to be resolved to an actual
     * type if/when the schema or type being read is ever used for validation.
     */
    val unresolvedReferences: MutableList<TypeArgument> = mutableListOf(),
    /**
     * Indicates whether the reader implementation should fail on the first error it encounters or it should continue
     * to try to report all errors.
     */
    val failFast: Boolean = false,
) {
    /**
     * Flag to indicate whether a header has been found yet.
     */
    var foundHeader: Boolean = false
    /**
     * Flag to indicate whether any type definition has been found yet.
     */
    var foundAnyType: Boolean = false

    /**
     * A list of errors encountered while reading a schema or type.
     */
    private val _readErrors: MutableList<ReadError> = mutableListOf()

    /**
     * Public, non-mutable view of the [ReadError]s collected in this [ReaderContext].
     */
    val readErrors: List<ReadError>
        get() = _readErrors.toList()

    /**
     * Reports a [ReadError] to this [ReaderContext].
     * If [failFast] is false, adds an error to this [ReaderContext].
     * If [failFast] is true, throws the error as an [InvalidSchemaException].
     */
    fun reportError(error: ReadError) {
        if (failFast) {
            InvalidSchemaException.failFast(error)
        } else {
            _readErrors.add(error)
        }
    }
}
