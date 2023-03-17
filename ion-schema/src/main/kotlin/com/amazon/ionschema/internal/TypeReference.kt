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
import com.amazon.ionschema.internal.util.IonSchema_2_0
import com.amazon.ionschema.internal.util.getIslRequiredField
import com.amazon.ionschema.internal.util.islRequire
import com.amazon.ionschema.internal.util.islRequireNotNull
import com.amazon.ionschema.internal.util.islRequireOnlyExpectedFieldNames

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

            when (schema.ionSchemaLanguageVersion) {
                IonSchemaVersion.v1_0 -> validateIonSchema1TypeReference(ion, variablyOccurring)
                IonSchemaVersion.v2_0 -> validateIonSchema2TypeReference(ion, variablyOccurring, isNamePermitted, allowedAnnotations)
            }

            val theType = when (ion) {
                is IonStruct -> {
                    if (ion.containsKey("id")) {
                        handleInlineImport(ion, schema, referenceManager)
                    } else {
                        handleInlineTypeDefinition(ion, schema, referenceManager, isField)
                    }
                }
                is IonSymbol -> handleSymbol(ion, schema, referenceManager)
                else -> throw InvalidSchemaException("Unable to resolve type reference '$ion'")
            }

            // Handle any nullability annotations
            val maybeNullableType = when {
                ion.hasTypeAnnotation("nullable") -> TypeNullable(ion, theType, schema)
                ion.hasTypeAnnotation("\$null_or") -> TypeOrNullDecorator(ion, theType, schema)
                else -> theType
            }

            return { maybeNullableType }
        }

        private fun handleInlineTypeDefinition(ion: IonStruct, schema: SchemaInternal, referenceManager: DeferredReferenceManager, isField: Boolean): TypeInternal {
            return when {
                isField -> TypeImpl(ion, schema, referenceManager)
                // elide inline types defined as "{ type: X }" to TypeImpl;
                // this avoids creating a nested, redundant validation structure
                ion.size() == 1 && ion["type"] != null -> TypeImpl(ion, schema, referenceManager)
                else -> TypeInline(ion, schema, referenceManager)
            }
        }

        private fun handleInlineImport(ion: IonStruct, thisSchema: SchemaInternal, referenceManager: DeferredReferenceManager): TypeInternal {
            val schemaSystem = thisSchema.getSchemaSystem()
            val schemaId: String = ion.getIslRequiredField<IonText>("id").stringValue()
            val typeId: IonSymbol = ion.getIslRequiredField("type")

            if (thisSchema.ionSchemaLanguageVersion >= IonSchemaVersion.v2_0) {
                ion.islRequireOnlyExpectedFieldNames(IonSchema_2_0.INLINE_IMPORT_KEYWORDS)
                val thisSchemaId = thisSchema.schemaId
                islRequire(schemaId != thisSchemaId) { "A schema may not directly import itself: $ion" }
            }

            return if (schemaSystem.doesSchemaDeclareType(schemaId, typeId)) {
                referenceManager.createDeferredImportReference(schemaId, typeId)
            } else if (schemaSystem.getParam(IonSchemaSystemImpl.Param.ALLOW_TRANSITIVE_IMPORTS)) {
                // Even though the type is not declared in the given schema, it could still be a transitive import if
                // those are enabled. There is no support for circular dependency chains that include transitive imports,
                // so we'll try to load the actual type without concern for whether it is properly deferred.
                val importedSchema = runCatching { schemaSystem.loadSchema(schemaId) }
                    .getOrElse { e -> throw InvalidSchemaException("Unable to load schema '$schemaId'; ${e.message}") }
                islRequireNotNull(importedSchema.getType(typeId.stringValue())) { "No such type $typeId in schema $schemaId" }
            } else {
                throw InvalidSchemaException("No type named '$typeId' found in $schemaId")
            }
        }

        private fun handleSymbol(ion: IonSymbol, schema: SchemaInternal, referenceManager: DeferredReferenceManager): TypeInternal {
            val t = schema.getInScopeType(ion.stringValue())

            return if (t != null) {
                t as? TypeBuiltin ?: TypeNamed(ion, t)
            } else {
                // type can't be resolved yet;  ask the schema to try again later
                referenceManager.createDeferredLocalReference(schema, ion)
            }
        }

        /**
         * Checks [ion] against the syntax for Ion Schema 1.0
         */
        private fun validateIonSchema1TypeReference(ion: IonValue, variablyOccurring: Boolean) {
            if (variablyOccurring && ion is IonStruct && ion.containsKey("occurs")) {
                islRequire(!ion.hasTypeAnnotation("nullable")) { "'nullable::' is not allowed on variably occurring types" }
            }
        }

        /**
         * Checks [ion] against the syntax for Ion Schema 2.0
         */
        private fun validateIonSchema2TypeReference(
            ion: IonValue,
            variablyOccurring: Boolean,
            isNamePermitted: Boolean,
            allowedAnnotations: Set<String>
        ) {
            islRequire(isNamePermitted || !ion.hasTypeAnnotation("type")) {
                "'type::' annotation not allowed on type references: $ion"
            }

            val illegalAnnotations = ion.typeAnnotations.filter { it !in allowedAnnotations }
            islRequire(illegalAnnotations.isEmpty()) { "Illegal annotations $illegalAnnotations on type reference: $ion" }

            if (ion is IonStruct) {
                islRequire(isNamePermitted || !ion.containsKey("name")) {
                    "Illegal 'name' field in type reference: $ion"
                }

                // It can only have 'occurs' if it's allowed to be a variable occurring type
                islRequire(variablyOccurring || !ion.containsKey("occurs")) {
                    "Variably occurring type reference not permitted: $ion"
                }

                // If it does have 'occurs' then it may not have `$null_or`
                islRequire(!ion.containsKey("occurs") || !ion.hasTypeAnnotation("\$null_or")) {
                    "'\$null_or::' is not allowed on variably occurring types"
                }
            }
        }
    }
}
