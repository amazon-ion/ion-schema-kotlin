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
import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonText
import com.amazon.ion.IonValue
import com.amazon.ionschema.Import
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.LogLevel.Warn
import com.amazon.ionschema.Schema
import com.amazon.ionschema.Type
import com.amazon.ionschema.Violations
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

    private val declaredTypes: Map<String, TypeImpl>

    init {
        val dgIsl = schemaSystem.ionSystem.newDatagram()

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

            resolveDeferredTypeReferences()
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
        declaredTypes = types.values.filterIsInstance<TypeImpl>().associateBy { it.name }

        if (declaredTypes.isEmpty()) {
            schemaSystem.logger(Warn) { "Schema declares no types -- '$schemaId'" }
        }
    }

    private class SchemaAndTypeImports(val id: String, val schema: Schema) {
        var types: MutableMap<String, Type> = mutableMapOf()

        fun addType(name: String, type: Type) {
            types[name]?.let {
                if (it.schemaId != type.schemaId || it.isl != type.isl) {
                    throw InvalidSchemaException("Duplicate imported type name/alias encountered: '$name'")
                } else if (it is ImportedType && it.schemaId == it.importedFromSchemaId) {
                    return@addType
                }
            }
            types[name] = type
        }
    }

    private fun loadHeader(
        typeMap: MutableMap<String, Type>,
        header: IonStruct
    ): Map<String, Import> {

        val importsMap = mutableMapOf<String, SchemaAndTypeImports>()
        val importSet: MutableSet<String> = schemaSystem.getSchemaImportSet()
        val allowTransitiveImports = schemaSystem.getParam(IonSchemaSystemImpl.Param.ALLOW_TRANSITIVE_IMPORTS)

        (header.get("imports") as? IonList)
            ?.filterIsInstance<IonStruct>()
            ?.forEach {
                val childImportId = it["id"] as IonText
                val alias = it["as"] as? IonSymbol
                // if importSet has an import with this id then do not load schema again to break the cycle.
                if (!importSet.contains(childImportId.stringValue())) {
                    var parentImportId = schemaId ?: ""

                    // if Schema is importing itself then throw error
                    if (parentImportId.equals(childImportId.stringValue())) {
                        throw InvalidSchemaException("Schema can not import itself.")
                    }

                    // add parent and current schema to importSet and continue loading current schema
                    importSet.add(parentImportId)
                    importSet.add(childImportId.stringValue())
                    val importedSchema = schemaSystem.loadSchema(childImportId.stringValue())
                    importSet.remove(childImportId.stringValue())
                    importSet.remove(parentImportId)

                    val schemaAndTypes = importsMap.getOrPut(childImportId.stringValue()) {
                        SchemaAndTypeImports(childImportId.stringValue(), importedSchema)
                    }

                    val typeName = (it["type"] as? IonSymbol)?.stringValue()
                    if (typeName != null) {
                        var importedType = importedSchema.getDeclaredType(typeName)

                        if (importedType == null && allowTransitiveImports) {
                            importedType = importedSchema.getType(typeName)?.also { type ->
                                schemaSystem.logger(Warn) {
                                    "Import is resolved transitively; $typeName is actually declared in " +
                                        "schema '${type.schemaId}' -- $it"
                                }
                            }
                        }

                        importedType ?: throw InvalidSchemaException("Schema $childImportId doesn't contain a type named '$typeName'")

                        importedType = decorateImportedType(
                            importedType as TypeInternal,
                            importedFromSchemaId = (importedSchema as SchemaImpl).schemaId!!,
                            importedToSchemaId = this.schemaId ?: "<unnamed schema>"
                        )

                        if (alias != null) {
                            importedType = TypeAliased(alias, importedType)
                        }
                        addType(typeMap, importedType)
                        schemaAndTypes.addType(alias?.stringValue() ?: typeName, importedType)
                    } else {
                        val typesToAdd =
                            if (allowTransitiveImports)
                                importedSchema.getTypes()
                            else
                                importedSchema.getDeclaredTypes()

                        typesToAdd.asSequence()
                            .map { type ->
                                decorateImportedType(
                                    type as TypeInternal,
                                    importedFromSchemaId = (importedSchema as SchemaImpl).schemaId!!,
                                    importedToSchemaId = this.schemaId ?: "<unnamed schema>"
                                )
                            }
                            .forEach { type ->
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
        if (!schemaSystem.getParam(IonSchemaSystemImpl.Param.ALLOW_ANONYMOUS_TOP_LEVEL_TYPES)) {
            val name = (type.isl as IonStruct)["name"]
            if (name == null || name.isNullValue) {
                throw InvalidSchemaException(
                    "Top-level types of a schema must have a name ($type.isl)"
                )
            }
        }
    }

    private fun addType(typeMap: MutableMap<String, Type>, type: Type) {
        validateType(type)
        getType(type.name)?.let {
            if (it.schemaId != type.schemaId || it.isl != type.isl) {
                throw InvalidSchemaException("Duplicate type name/alias encountered: '${it.name}'")
            }
            // If there are duplicate types, one might be a non-imported type or a direct import.
            // We try to replace any transitive imported types with those better ones so that we
            // can be smarter about transitive import log warnings when a type is imported by
            // more than one path.
            if (it !is ImportedType || it.schemaId == it.importedFromSchemaId) {
                return@addType
            }
        }
        typeMap[type.name] = type
    }

    override fun getType(name: String) = schemaCore.getType(name) ?: types[name]

    override fun getDeclaredType(name: String) = declaredTypes[name]

    override fun getDeclaredTypes(): Iterator<Type> = declaredTypes.values.iterator()

    override fun getTypes(): Iterator<Type> =
        (schemaCore.getTypes().asSequence() + types.values.asSequence())
            .filter { it is ImportedType || it is TypeImpl }
            .iterator()

    override fun newType(isl: String) = newType(
        schemaSystem.ionSystem.singleValue(isl) as IonStruct
    )

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
        val newIsl = schemaSystem.ionSystem.newDatagram()
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
                "Unable to resolve type reference(s): $unresolvedDeferredTypeReferences"
            )
        }
    }

    /**
     * Returns a new [ImportedType] instance that decorates [type] so that it will
     * log a transitive import warning every time it is used for validation.
     */
    private fun decorateImportedType(
        type: TypeInternal,
        importedFromSchemaId: String,
        importedToSchemaId: String
    ) = object : ImportedType, TypeInternal by type {
        override fun validate(value: IonValue, issues: Violations) {
            if (importedFromSchemaId != schemaId)
                schemaSystem.logger(Warn) {
                    "Use of transitively imported type '$name'" +
                        " in '$importedToSchemaId'. '$name' is actually from '$schemaId', " +
                        "but was imported from '$importedFromSchemaId'"
                }
            type.validate(value, issues)
        }

        override val importedFromSchemaId: String
            get() = importedFromSchemaId
    }
}
