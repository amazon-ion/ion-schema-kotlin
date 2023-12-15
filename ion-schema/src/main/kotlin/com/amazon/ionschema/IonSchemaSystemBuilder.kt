/*
 * Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.ionschema

import com.amazon.ion.IonSystem
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.internal.ConstraintFactoryDefault
import com.amazon.ionschema.internal.IonSchemaSystemImpl
import com.amazon.ionschema.internal.WarningType
import com.amazon.ionschema.util.DefaultRegexImplementation
import com.amazon.ionschema.util.RegexImplementation
import java.util.function.Consumer

/**
 * Entry point for Ion Schema.  Provides a builder API for constructing
 * [IonSchemaSystem]s using the specified [Authority]s and IonSystem.
 */
class IonSchemaSystemBuilder private constructor() {
    companion object {
        /**
         * Provides a standard instance of IonSchemaSystemBuilder.
         */
        @JvmStatic
        fun standard() = IonSchemaSystemBuilder()

        private val defaultConstraintFactory = ConstraintFactoryDefault
    }

    private var authorities = mutableListOf<Authority>()
    private var constraintFactory = defaultConstraintFactory
    private var ionSystem = IonSystemBuilder.standard().build()
    private var schemaCache: SchemaCache? = null
    private var params = mutableMapOf<IonSchemaSystemImpl.Param<*>, Any>()
    private var warningCallback: ((() -> String) -> Unit)? = null
    private var regexImplementation: RegexImplementation = DefaultRegexImplementation

    /**
     * Adds the provided authority to the list of [Authority]s.
     */
    fun addAuthority(authority: Authority): IonSchemaSystemBuilder {
        authorities.add(authority)
        return this
    }

    /**
     * Replaces the list of [Authority]s with a list containing only
     * the specified authority.
     */
    fun withAuthority(authority: Authority): IonSchemaSystemBuilder {
        this.authorities = mutableListOf(authority)
        return this
    }

    /**
     * Replaces the list of [Authority]s with the specified list of [Authority]s.
     */
    fun withAuthorities(authorities: List<Authority>): IonSchemaSystemBuilder {
        this.authorities = mutableListOf<Authority>().apply { addAll(authorities) }
        return this
    }

    /**
     * Provides the IonSystem to use when building an IonSchemaSystem.
     */
    fun withIonSystem(ionSystem: IonSystem): IonSchemaSystemBuilder {
        this.ionSystem = ionSystem
        return this
    }

    /**
     * Provides the SchemaCache to use when building an IonSchemaSystem.
     */
    fun withSchemaCache(schemaCache: SchemaCache): IonSchemaSystemBuilder {
        this.schemaCache = schemaCache
        return this
    }

    /**
     * Allows top-level types to not have a name.  Such types can't be referred to
     * by name and are thereby of limited (if any?) value.  This option if provided
     * in case consumers have defined top-level types that don't have names.
     * Should only be used if needed for backwards compatibility with v1.0; this will
     * be removed in a future release.
     *
     * @since 1.1
     */
    @Deprecated("For backwards compatibility with v1.0")
    fun allowAnonymousTopLevelTypes(): IonSchemaSystemBuilder {
        params.put(IonSchemaSystemImpl.Param.ALLOW_ANONYMOUS_TOP_LEVEL_TYPES, true)
        return this
    }

    /**
     * Allows forward-compatibility with the fixed, spec-compliant behavior for
     * handling schema imports that is introduced in `ion-schema-kotlin-2.0.0`.
     * If not set, the default value is `true`.
     *
     * When set to `true`, the `IonSchemaSystem` will generate warnings about
     * usages of transitive imports, which can be consumed by configuring the
     * `IonSchemaSystem` with a callback function using [withWarningMessageCallback].
     *
     * Before setting this option to `false`, you should verify that your schemas
     * will continue to work as intended, and you may need to update schemas that
     * rely on the current, incorrect import resolution logic.
     *
     * @since 1.2
     * @see [WarningType.INVALID_TRANSITIVE_IMPORT]
     */
    fun allowTransitiveImports(boolean: Boolean): IonSchemaSystemBuilder {
        params[IonSchemaSystemImpl.Param.ALLOW_TRANSITIVE_IMPORTS] = boolean
        return this
    }

    /**
     * Causes the evaluation of a value against a type definition to return early if the value is
     * invalid for the given `annotations` constraint, if one is present. If unset, this option
     * defaults to `false`.
     *
     * ### Rationale
     *
     * If you use annotations as application-defined type tags on Ion values, this option can benefit
     * you in two ways—it will reduce noise in the [Violations] data when calling [Type.validate],
     * and it can result in performance improvements, especially in cases where the type definition
     * includes constraints such as `element`, `fields` and `ordered_elements` which constrain child
     * values of a container.
     *
     * ### Example
     *
     * Consider the following type definitions (which are functionally equivalent for Ion Schema 1.0 and 2.0).
     * ```
     * type::{
     *   name: foo_struct,
     *   annotations: closed::required::[foo],
     *   fields: {
     *     id: { occurs: required, type: string, regex: "^[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}$" }
     *     a: int,
     *     b: bool,
     *   }
     * }
     * type::{
     *   name: bar_struct,
     *   annotations: closed::required::[foo],
     *   fields: {
     *     id: { occurs: required, type: string, regex: "^[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}$" }
     *     b: int,
     *     c: timestamp,
     *   }
     * }
     * type::{
     *   name: foobar,
     *   one_of: [foo_struct, bar_struct],
     * }
     * ```
     * With this feature enabled, when validating a value such as `bar::{ id: "qwerty" }` against
     * the `foobar` type, the comparison with the `foo_struct` as part of the `one_of` constraint
     * will not check all the fields in the struct because the struct does not match the annotations
     * of the `foo_struct` type. If you were to validate a value such as `quux::{ id: "qwerty" }`,
     * there would be no validation of the fields at all because the annotation `quux` does not match
     * the annotations of `foo_struct` or `bar_struct`.
     *
     * @since 1.7
     */
    fun failFastOnInvalidAnnotations(boolean: Boolean): IonSchemaSystemBuilder {
        params[IonSchemaSystemImpl.Param.SHORT_CIRCUIT_ON_INVALID_ANNOTATIONS] = boolean
        return this
    }

    /**
     * Provides a callback for the IonSchemaSystem to send a warning message about
     * things that are not fatal (i.e. will not result in an exception being thrown).
     * Content of the messages may include information about possible errors in
     * schemas, and usage of features that may be deprecated in future versions of
     * the Ion Schema Language or the `ion-schema-kotlin` library.
     *
     * Clients can use these warnings as they please. Some possible uses are:
     * - Log the warning messages
     * - Surface the warnings to an end user who is authoring schemas
     * - Enforce a "strict mode" by throwing an exception for warnings (ie. like
     *   `javac -Werror`, but for Ion Schemas)
     *
     * @since 1.2
     */
    fun withWarningMessageCallback(callbackFn: Consumer<String>): IonSchemaSystemBuilder {
        this.warningCallback = { callbackFn.accept(it()) }
        return this
    }

    /**
     * Provides a callback for the IonSchemaSystem to send a warning message about
     * things that are not fatal (i.e. will not result in an exception being thrown).
     * Content of the messages may include information about possible errors in
     * schemas, and usage of features that may be deprecated in future versions of
     * the Ion Schema Language or the `ion-schema-kotlin` library.
     *
     * Clients can use these warnings as they please. Some possible uses are:
     * - Log the warning messages
     * - Surface the warnings to an end user who is authoring schemas
     * - Enforce a "strict mode" by throwing an exception for warnings (ie. like
     *   `javac -Werror`, but for Ion Schemas)
     *
     * @since 1.2
     */
    fun withWarningMessageCallback(callbackFn: (String) -> Unit): IonSchemaSystemBuilder {
        this.warningCallback = { callbackFn(it()) }
        return this
    }

    /**
     * Sets the regex implementation to be used by the [IonSchemaSystem].
     *
     * This can be used to replace the regex implementation in the Java standard library with an implementation of your
     * own choosing. You might want to provide your own [RegexImplementation] in order to be able to set a timeout for
     * evaluating inputs against a pattern, or to use an algorithm with different time or space complexity.
     *
     * For example, if you are accepting input from untrusted sources, you may choose to use a linear time algorithm for
     * finding matches in order to protect against potential ReDoS attacks using
     * [catastrophic backtracking](https://www.regular-expressions.info/catastrophic.html).
     *
     * See [AlternateRegexImplementationTest.kt](https://github.com/amazon-ion/ion-schema-kotlin/blob/master/ion-schema/src/test/kotlin/com/amazon/ionschema/AlternateRegexImplementationTest.kt)
     * for an example of how one might implement [RegexImplementation] using a linear-time regex library.
     *
     * **WARNING**—if you supply your own [RegexImplementation] that differs from the ECMA standard, it may result in
     * unexpected behavior when validation Ion data.
     */
    fun withRegexImplementation(regexImplementation: RegexImplementation): IonSchemaSystemBuilder {
        this.regexImplementation = regexImplementation
        return this
    }

    /**
     * Instantiates an [IonSchemaSystem] using the provided [Authority](s)
     * and IonSystem.
     */
    fun build(): IonSchemaSystem = IonSchemaSystemImpl(
        ionSystem,
        authorities,
        constraintFactory,
        schemaCache ?: SchemaCacheDefault(),
        params,
        (warningCallback ?: { }),
        regexImplementation,
    )
}
