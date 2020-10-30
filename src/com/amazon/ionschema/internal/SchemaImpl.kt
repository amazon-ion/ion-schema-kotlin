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
import com.amazon.ion.IonText
import com.amazon.ion.IonValue
import com.amazon.ionschema.Import
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.IonSchemaException
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
        val schemaId: String?,
        preloadedImports: Map<String, Import>,
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
        schemaContent: Iterator<IonValue>,
        schemaId: String?
    ) : this(schemaSystem, schemaCore, schemaContent, schemaId, emptyMap(), mutableMapOf())

    private val deferredTypeReferences = mutableListOf<TypeReferenceDeferred>()

    override val isl: IonDatagram

    private val imports: Map<String, Import>

    var nocycle = false // this flag helps in not loading types when there is a cycle

    init {
        val dgIsl = schemaSystem.getIonSystem().newDatagram()

        if (types.isEmpty()) {
            var foundHeader = false
            var foundFooter = false
            var importsMap = emptyMap<String, Import>()

            while (schemaContent.hasNext()) {
                val it = schemaContent.next()

                dgIsl.add(it.clone())

                if (it is IonSymbol && it.stringValue() == "\$ion_schema_1_0") {
                    // TBD https://github.com/amzn/ion-schema-kotlin/issues/95

                } else if (it.hasTypeAnnotation("schema_header")) {
                    importsMap = loadHeader(types, it as IonStruct)
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
            if(nocycle) {
                resolveDeferredTypeReferences()
            }
            imports = importsMap

        } else {
            // in this case the new Schema is based on an existing Schema and the 'types'
            // map was populated by the caller
            schemaContent.forEach {
                dgIsl.add(it.clone())
            }
            imports = preloadedImports
        }

        isl = dgIsl.markReadOnly()
    }

    private class SchemaAndTypeImports(val id: String, val schema: Schema) {
        var types: MutableMap<String,Type> = mutableMapOf()

        fun addType(name: String, type: Type) {
            types[name]?.let {
                if (it.schemaId != type.schemaId) {
                    throw InvalidSchemaException("Duplicate imported type name/alias encountered: '$name'")
                }
                return@addType
            }
            types[name] = type
        }
    }

    private fun loadHeader(typeMap: MutableMap<String, Type>,
                           header: IonStruct): Map<String, Import> {

        val importsMap = mutableMapOf<String, SchemaAndTypeImports>()
        val importSet: MutableSet<String> = schemaSystem.getSchemaImportSet()

        (header.get("imports") as? IonList)
            ?.filterIsInstance<IonStruct>()
            ?.forEach {
                val id = it["id"] as IonText
                val alias = it["as"] as? IonSymbol
                // if importSet has an import with this id then do not load schema again to break the cycle.
                if(!importSet.contains(id.stringValue())) {
                    nocycle = true

                    // normalize schemaId to have .isl at the end as importSet need a common pattern to be followed
                    // for Ids added into the set
                    var normalizedId = ""
                    if(schemaId != null) {
                        normalizedId = if(schemaId.endsWith(".isl")) schemaId else schemaId + ".isl"
                    }

                    // if Schema is importing itself then throw error
                    if(normalizedId.equals(id.stringValue())) {
                        throw InvalidSchemaException("Schema can not import itself.")
                    }


                    // add parent and current schema to importSet and continue loading current schema
                    schemaSystem.addToSchemaImportSet(normalizedId)
                    schemaSystem.addToSchemaImportSet(id.stringValue())
                    val importedSchema = schemaSystem.loadSchema(id.stringValue())
                    schemaSystem.resetSchemaImportSet(id.stringValue(), normalizedId)

                    val schemaAndTypes = importsMap.getOrPut(id.stringValue()) {
                        SchemaAndTypeImports(id.stringValue(), importedSchema)
                    }

                    val typeName = (it["type"] as? IonSymbol)?.stringValue()
                    if (typeName != null) {
                        var newType = importedSchema.getType(typeName)
                                ?: throw InvalidSchemaException(
                                        "Schema $id doesn't contain a type named '$typeName'")

                        if (alias != null) {
                            newType = TypeAliased(alias, newType as TypeInternal)
                        }
                        addType(typeMap, newType)
                        schemaAndTypes.addType(alias?.stringValue() ?: typeName, newType)
                    } else {
                        importedSchema.getTypes().forEach { type ->
                            addType(typeMap, type)
                            schemaAndTypes.addType(type.name, type)
                        }
                    }
                }
            }
        return importsMap.mapValues {
            ImportImpl(it.value.id, it.value.schema, it.value.types)
        }
    }

    override fun getImport(id: String) = imports[id]

    override fun getImports() = imports.values.iterator()

    private fun validateType(type: Type) {
        if (!schemaSystem.hasParam(IonSchemaSystemImpl.Param.ALLOW_ANONYMOUS_TOP_LEVEL_TYPES)) {
            val name = (type.isl as IonStruct)["name"]
            if (name == null || name.isNullValue) {
                throw InvalidSchemaException(
                        "Top-level types of a schema must have a name ($type.isl)")
            }
        }
    }

    private fun addType(typeMap: MutableMap<String, Type>, type: Type) {
        validateType(type)
        getType(type.name)?.let {
            if (it.schemaId != type.schemaId) {
                throw InvalidSchemaException("Duplicate type name/alias encountered: '${it.name}'")
            }
            return@addType
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
        return SchemaImpl(schemaSystem, schemaCore, newIsl.iterator(), null, imports, preLoadedTypes)
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

