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

package com.amazon.ionschema.internal

import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonText
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.Violations
import com.amazon.ionschema.internal.util.IonSchema_2_0
import com.amazon.ionschema.internal.util.getIslOptionalField
import com.amazon.ionschema.internal.util.getIslRequiredField
import com.amazon.ionschema.internal.util.islRequire
import com.amazon.ionschema.internal.util.islRequireOnlyExpectedFieldNames
import com.amazon.ionschema.internal.util.markReadOnly

/**
 * Provides a factory method that translates an ISL type reference into a function
 * that returns a Type instance.
 *
 * Types that can't be resolved yet are instantiated as [TypeReferenceDeferred] objects
 * that are resolved by [SchemaImpl.resolveDeferredTypeReferences] prior to asserting
 * that the schema is valid.
 */
internal class TypeReference private constructor() {
    companion object {
        val DEFAULT_ALLOWED_ANNOTATIONS = setOf("\$null_or")

        fun create(
            ion: IonValue,
            schema: SchemaInternal,
            referenceManager: DeferredReferenceManager,
            isField: Boolean = false,
            variablyOccurring: Boolean = false,
            isNamePermitted: Boolean = false,
            allowedAnnotations: Set<String> = DEFAULT_ALLOWED_ANNOTATIONS
        ): () -> TypeInternal {

            if (ion.isNullValue) {
                throw InvalidSchemaException("Unable to resolve type reference '$ion'")
            }

            if (schema.ionSchemaLanguageVersion != IonSchemaVersion.v1_0) {
                if (!isNamePermitted) islRequire(!ion.hasTypeAnnotation("type")) {
                    "'type::' annotation not allowed on type references: $ion"
                }
                val illegalAnnotations = ion.typeAnnotations.filter { it !in allowedAnnotations }
                islRequire(illegalAnnotations.isEmpty()) { "Illegal annotations $illegalAnnotations on type reference: $ion" }
            }

            return when (ion) {
                is IonStruct -> {
                    if (schema.ionSchemaLanguageVersion != IonSchemaVersion.v1_0) {
                        if (!variablyOccurring) islRequire(!ion.containsKey("occurs")) {
                            "Variably occurring type reference not permitted: $ion"
                        }
                        if (!isNamePermitted) islRequire(!ion.containsKey("name")) {
                            "Illegal 'name' field in type reference: $ion"
                        }
                    }
                    handleStruct(ion, schema, isField, referenceManager)
                }
                is IonSymbol -> handleSymbol(ion, schema)
                else -> throw InvalidSchemaException("Unable to resolve type reference '$ion'")
            }
        }

        private fun handleStruct(ion: IonStruct, schema: SchemaInternal, isField: Boolean, referenceManager: DeferredReferenceManager): () -> TypeInternal {
            val id = ion.getIslOptionalField<IonText>("id")
            val type = when {
                id != null -> {
                    // import
                    if (schema.ionSchemaLanguageVersion >= IonSchemaVersion.v2_0) {
                        ion.islRequireOnlyExpectedFieldNames(IonSchema_2_0.INLINE_IMPORT_KEYWORDS)
                        islRequire(id.stringValue() != schema.schemaId) { "A schema may not directly import itself: $ion" }
                    }
                    val typeName = ion.getIslRequiredField<IonSymbol>("type")
                    val importedSchema = runCatching { schema.getSchemaSystem().loadSchema(id.stringValue()) }
                        .getOrElse { e -> throw InvalidSchemaException("Unable to load schema '${id.stringValue()}'; ${e.message}") }
                    importedSchema.getType(typeName.stringValue())
                }
                isField -> TypeImpl(ion, schema, referenceManager)
                ion.size() == 1 && ion["type"] != null -> {
                    // elide inline types defined as "{ type: X }" to TypeImpl;
                    // this avoids creating a nested, redundant validation structure
                    TypeImpl(ion, schema, referenceManager)
                }
                else -> TypeInline(ion, schema, referenceManager)
            }

            type ?: throw InvalidSchemaException("Unable to resolve type reference '$ion'")

            val theType = handleNullable(ion, schema, type)
            return { theType }
        }

        private fun handleSymbol(ion: IonSymbol, schema: SchemaInternal): () -> TypeInternal {
            val t = schema.getType(ion.stringValue())
            return if (t != null) {
                val type = t as? TypeBuiltin ?: TypeNamed(ion, t)
                val theType = handleNullable(ion, schema, type);
                { theType }
            } else {
                // type can't be resolved yet;  ask the schema to try again later
                val deferredType = TypeReferenceDeferred(ion, schema)
                schema.addDeferredType(deferredType);
                { deferredType.resolve() }
            }
        }

        private fun handleNullable(ion: IonValue, schema: SchemaInternal, type: TypeInternal): TypeInternal {
            return when {
                ion.hasTypeAnnotation("nullable") -> TypeNullable(ion, type, schema)
                ion.hasTypeAnnotation("\$null_or") -> TypeOrNullDecorator(ion, type, schema)
                else -> type
            }
        }
    }
}

/**
 * Represents a type reference that can't be resolved yet.
 */
internal class TypeReferenceDeferred(
    nameSymbol: IonSymbol,
    private val schema: SchemaInternal
) : TypeInternal {

    private var type: TypeInternal? = null
    override val name: String = nameSymbol.stringValue()
    override val schemaId: String? = schema.schemaId
    override val isl = nameSymbol.markReadOnly()

    fun attemptToResolve(): Boolean {
        type = schema.getType(name)
        return type != null
    }

    fun resolve(): TypeInternal = type!!

    override fun getBaseType(): TypeBuiltin = throw UnsupportedOperationException()

    override fun isValidForBaseType(value: IonValue): Boolean = throw UnsupportedOperationException()

    override fun validate(value: IonValue, issues: Violations) = throw UnsupportedOperationException()
}
