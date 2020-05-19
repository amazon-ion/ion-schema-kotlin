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

import com.amazon.ion.IonDatagram
import com.amazon.ion.IonList
import com.amazon.ion.IonString
import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.Schema
import com.amazon.ionschema.Type
import com.amazon.ionschema.internal.util.markReadOnly

/**
 * Implementation of [Schema] for all user-provided ISL.
 */
internal class SchemaImpl private constructor(
        private val schemaSystem: IonSchemaSystemImpl,
        private val schemaCore: SchemaCore,
        schemaContent: Iterator<IonValue>,
        /*
         * [types] is declared as a MutableMap in order to be populated DURING
         * INITIALIZATION ONLY.  This enables type B to find its already-loaded
         * dependency type A.  After initialization, [types] is expected to
         * be treated as immutable as required by the Schema interface.
         */
        private val types: MutableMap<String, Type>
) : Schema {

    internal constructor(
        schemaSystem: IonSchemaSystemImpl,
        schemaCore: SchemaCore,
        schemaContent: Iterator<IonValue>
    ) : this(schemaSystem, schemaCore, schemaContent, mutableMapOf())

    private val deferredTypeReferences = mutableListOf<TypeReferenceDeferred>()

    override val isl: IonDatagram

    init {
        val dgIsl = schemaSystem.getIonSystem().newDatagram()

        if (types.isEmpty()) {
            var foundHeader = false
            var foundFooter = false

            while (schemaContent.hasNext()) {
                val it = schemaContent.next()

                dgIsl.add(it.clone())

                if (it is IonSymbol && it.stringValue() == "\$ion_schema_1_0") {
                    // TBD https://github.com/amzn/ion-schema-kotlin/issues/95

                } else if (it.hasTypeAnnotation("schema_header")) {
                    loadHeader(types, it as IonStruct)
                    foundHeader = true

                } else if (!foundFooter && it.hasTypeAnnotation("type") && it is IonStruct) {
                    val newType = TypeImpl(it, this)
                    addType(types, newType)

                } else if (it.hasTypeAnnotation("schema_footer")) {
                    foundFooter = true
                }
            }

            if (foundHeader && !foundFooter) {
                throw InvalidSchemaException("Found a schema_header, but not a schema_footer")
            }
            if (!foundHeader && foundFooter) {
                throw InvalidSchemaException("Found a schema_footer, but not a schema_header")
            }

            resolveDeferredTypeReferences()

        } else {
            // in this case the new Schema is based on an existing Schema and the 'types'
            // map was populated by the caller
            schemaContent.forEach {
                dgIsl.add(it.clone())
            }
        }

        isl = dgIsl.markReadOnly()
    }

    private fun loadHeader(typeMap: MutableMap<String, Type>, header: IonStruct) {
        (header.get("imports") as? IonList)
            ?.filterIsInstance<IonStruct>()
            ?.forEach {
                val id = it["id"] as IonString
                val importedSchema = schemaSystem.loadSchema(id.stringValue())

                val typeName = (it["type"] as? IonSymbol)?.stringValue()
                if (typeName != null) {
                    var newType = importedSchema.getType(typeName)
                            ?: throw InvalidSchemaException(
                                "Schema $id doesn't contain a type named '$typeName'")

                    val alias = it["as"] as? IonSymbol
                    if (alias != null) {
                        newType = TypeAliased(alias, newType as TypeInternal)
                    }
                    addType(typeMap, newType)
                } else {
                    importedSchema.getTypes().forEach {
                        addType(typeMap, it)
                    }
                }
            }
    }

    private fun validateType(type: Type) {
        val name = (type.isl as IonStruct)["name"]
        if (!schemaSystem.hasParam(IonSchemaSystemImpl.Param.ALLOW_UNNAMED_TOP_LEVEL_TYPES)
                && (name == null || name.isNullValue)) {
            throw InvalidSchemaException("Top-level types of a schema must have a name ($type.isl)")
        }
    }

    private fun addType(typeMap: MutableMap<String, Type>, type: Type) {
        validateType(type)
        if (getType(type.name) != null) {
            throw InvalidSchemaException("Duplicate type name/alias encountered:  '$type.name'")
        }
        typeMap[type.name] = type
    }

    override fun getType(name: String) = schemaCore.getType(name) ?: types[name]

    override fun getTypes(): Iterator<Type> =
            (schemaCore.getTypes().asSequence() + types.values.asSequence())
                    .filter { it is TypeNamed || it is TypeAliased || it is TypeImpl }
                    .iterator()

    override fun newType(isl: String) = newType(
            schemaSystem.getIonSystem().singleValue(isl) as IonStruct)

    override fun newType(isl: IonStruct): Type {
        val type = TypeImpl(isl, this)
        resolveDeferredTypeReferences()
        return type
    }

    override fun plusType(type: Type): Schema {
        validateType(type)

        // prepare ISL corresponding to the new Schema
        // (might be simpler if IonDatagram.set(int, IonValue) were implemented,
        // see https://github.com/amzn/ion-java/issues/50)
        val newIsl = schemaSystem.getIonSystem().newDatagram()
        var newTypeAdded = false
        isl.forEachIndexed { idx, value ->
            if (!newTypeAdded) {
                when {
                    value is IonStruct
                            && (value["name"] as? IonSymbol)?.stringValue().equals(type.name) -> {
                        // new type replaces existing type of the same name
                        newIsl.add(type.isl.clone())
                        newTypeAdded = true
                        return@forEachIndexed
                    }
                    (value is IonStruct && value.hasTypeAnnotation("schema_footer"))
                            || idx == isl.lastIndex -> {
                        newIsl.add(type.isl.clone())
                        newTypeAdded = true
                    }
                }
            }
            newIsl.add(value.clone())
        }

        // clone the types map:
        val preLoadedTypes = types.toMutableMap()
        preLoadedTypes[type.name] = type
        return SchemaImpl(schemaSystem, schemaCore, newIsl.iterator(), preLoadedTypes)
    }

    override fun getSchemaSystem() = schemaSystem

    internal fun addDeferredType(typeRef: TypeReferenceDeferred) {
        deferredTypeReferences.add(typeRef)
    }

    private fun resolveDeferredTypeReferences() {
        val unresolvedDeferredTypeReferences = deferredTypeReferences
                .filterNot { it.attemptToResolve() }
                .map { it.name }.toSet()

        if (unresolvedDeferredTypeReferences.isNotEmpty()) {
            throw InvalidSchemaException(
                    "Unable to resolve type reference(s): $unresolvedDeferredTypeReferences")
        }
    }
}

