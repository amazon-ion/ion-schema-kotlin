/*
 * Copyright 2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazon.ionschema.internal

import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.Violations

/**
 * The classes in this file are an implementation detail of [DeferredReferenceManager], and should not be used outside
 * [DeferredReferenceManager] except for testing. To properly encapsulate [DeferredReference] and its implementations,
 * we would have to make them private inside [DeferredReferenceManager], but that makes it difficult to thoroughly test
 * these classes. In order to directly test the private classes, we would have to use reflection to modify the class
 * visibility in the unit tests. However, this will not work if the ClassLoader has a SecurityManager that prohibits
 * changing visibility, and so the tests would be brittle.
 *
 * Instead, we have a [RequiresOptIn] annotation for all classes in this file. If you try to use [DeferredReference] or
 * any implementations without explicitly opting in, it will result in a compile-time error. Opting in requires
 *
 * ```
 * @OptIn(DeferredReferenceManagerImplementationDetails::class)
 * ```
 *
 * Essentially, this is like a much stricter version of the @VisibleForTesting annotation that is commonly used in Java.
 */
@RequiresOptIn(
    message = "Do not use DeferredReference and its implementations outside DeferredReferenceManager or unit tests.",
    level = RequiresOptIn.Level.ERROR
)
@Retention(AnnotationRetention.BINARY)
internal annotation class DeferredReferenceManagerImplementationDetails

/**
 * Represents a named ISL type that is known to be declared in a schema document, but it is not known whether an
 * instance of [TypeInternal] has been created for that ISL type yet.
 *
 * Concrete instances of [DeferredReference] should only be created by an instance of [DeferredReferenceManager]. The
 * [DeferredReferenceManager] ensures that all [DeferredReference]s are resolved before they are used for validation.
 *
 * Before it is resolved, any attempt to use validation functions will throw an [IllegalStateException]. Once it is
 * resolved, a [DeferredReference] acts like an ordinary [TypeInternal].
 */
@DeferredReferenceManagerImplementationDetails
internal interface DeferredReference : TypeInternal {
    override val isl: IonSymbol
    override val name: String
        get() = isl.stringValue()

    /**
     * Returns the [TypeInternal] that this [DeferredReference] resolves to.
     * If it cannot be resolved, throws [InvalidSchemaException].
     */
    fun resolve(): TypeInternal

    /**
     * Returns true if this [DeferredReference] is already resolved.
     */
    fun isResolved(): Boolean

    companion object {
        private fun illegalMethodCallBeforeResolving(): Nothing {
            throw IllegalStateException("This should be unreachable because this method should not be called before the call to DeferredReferenceManager.resolve() is complete.")
        }
    }
    override fun getBaseType(): TypeBuiltin = if (isResolved()) resolve().getBaseType() else illegalMethodCallBeforeResolving()
    override fun isValidForBaseType(value: IonValue): Boolean = if (isResolved()) resolve().isValidForBaseType(value) else illegalMethodCallBeforeResolving()
    override fun validate(value: IonValue, issues: Violations) = if (isResolved()) resolve().validate(value, issues) else illegalMethodCallBeforeResolving()
}

/**
 * An implementation of [DeferredReference] that refers to a [TypeInternal] that is declared in a [SchemaInternal]
 * that has already been instantiated.
 *
 * Use this for references to other types that are in scope for the same schema that the reference is being created for.
 */
@DeferredReferenceManagerImplementationDetails
internal class DeferredLocalReference(
    override val isl: IonSymbol,
    private val schema: SchemaInternal
) : DeferredReference {
    override val schemaId: String? = schema.schemaId
    private var type: TypeInternal? = null

    override fun resolve(): TypeInternal {
        return type
            ?: schema.getType(name)?.also { type = it }
            ?: throw InvalidSchemaException("Unable to resolve type $isl")
    }
    override fun isResolved(): Boolean = type != null
}

/**
 * An implementation of [DeferredReference] that refers to a [TypeInternal] that is declared in a [SchemaInternal]
 * that has not necessarily been instantiated yet. This class also implements [ImportedType], which means that
 * [schemaId] is required to be non-null.
 *
 * Use this for references to other types that are defined in other schemas.
 */
@DeferredReferenceManagerImplementationDetails
internal class DeferredImportReference(
    override val isl: IonSymbol,
    override val schemaId: String,
    private val schemaProvider: () -> SchemaInternal
) : DeferredReference, ImportedType {
    override val importedFromSchemaId: String
        get() = schemaId

    private var type: TypeInternal? = null

    override fun resolve(): TypeInternal {
        return type
            ?: schemaProvider().getType(name)?.also { type = it }
            ?: throw InvalidSchemaException("Unable to resolve type $isl")
    }
    override fun isResolved(): Boolean = type != null
}
