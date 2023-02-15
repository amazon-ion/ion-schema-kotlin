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
import com.amazon.ionschema.IonSchemaException
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.Schema
import com.amazon.ionschema.Type
import com.amazon.ionschema.Violations
import com.amazon.ionschema.internal.util.markReadOnly

/**
 * Implementation of [Schema] for all user-provided ISL.
 */
internal class SchemaImpl_1_0 private constructor(
    referenceManager: DeferredReferenceManager,
    private val schemaSystem: IonSchemaSystemImpl,
    private val schemaCore: SchemaCore,
    schemaContent: Iterator<IonValue>,
    override val schemaId: String?,
    preloadedImports: Map<String, Import>,
        /*
         * [types] is declared as a MutableMap in order to be populated DURING
         * INITIALIZATION ONLY.  This enables type B to find its already-loaded
         * dependency type A.  After initialization, [types] is expected to
         * be treated as immutable as required by the Schema interface.
         */
    private val types: MutableMap<String, TypeInternal>
) : SchemaInternal {

    internal constructor(
        referenceManager: DeferredReferenceManager,
        schemaSystem: IonSchemaSystemImpl,
        schemaCore: SchemaCore,
        schemaContent: Iterator<IonValue>,
        schemaId: String?
    ) : this(referenceManager, schemaSystem, schemaCore, schemaContent, schemaId, emptyMap(), mutableMapOf())

    override val isl: IonDatagram

    private val imports: Map<String, Import>

    private val declaredTypes: Map<String, TypeImpl>

    override val ionSchemaLanguageVersion: IonSchemaVersion
        get() = IonSchemaVersion.v1_0

    init {
        val dgIsl = schemaSystem.ionSystem.newDatagram()

        if (types.isEmpty()) {
            var foundHeader = false
            var foundFooter = false
            var importsMap = emptyMap<String, Import>()

            while (schemaContent.hasNext()) {
                val it = schemaContent.next()

                dgIsl.add(it.clone())

                if (IonSchemaVersion.isVersionMarker(it)) {
                    // This implementation only supports Ion Schema 1.0
                    if (it.stringValue() != "\$ion_schema_1_0") {
                        throw InvalidSchemaException("Unsupported Ion Schema version: ${it.stringValue()}")
                    }
                } else if (it.hasTypeAnnotation("schema_header")) {
                    importsMap = loadHeader(types, it as IonStruct, referenceManager)
                    foundHeader = true
                } else if (!foundFooter && it.hasTypeAnnotation("type") && it is IonStruct) {
                    val newType = TypeImpl(it, this, referenceManager)
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
            schemaSystem.emitWarning { "${WarningType.SCHEMA_HAS_NO_TYPES} -- '$schemaId'" }
        }
    }

    private class SchemaAndTypeImports(val id: String, val schema: () -> Schema) {
        var types: MutableMap<String, TypeInternal> = mutableMapOf()

        fun addType(name: String, type: TypeInternal) {
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
        typeMap: MutableMap<String, TypeInternal>,
        header: IonStruct,
        referenceManager: DeferredReferenceManager
    ): Map<String, Import> {

        val importsMap = mutableMapOf<String, SchemaAndTypeImports>()
        val allowTransitiveImports = schemaSystem.getParam(IonSchemaSystemImpl.Param.ALLOW_TRANSITIVE_IMPORTS)

        (header.get("imports") as? IonList)
            ?.filterIsInstance<IonStruct>()
            ?.forEach {
                val childImportId = (it["id"] as IonText).stringValue()
                val alias = it["as"] as? IonSymbol

                // if Schema is importing itself then throw error
                if (schemaId == childImportId) {
                    throw InvalidSchemaException("Schema can not import itself.")
                }

                val importedSchemaFn = {
                    try {
                        schemaSystem.loadSchema(childImportId)
                    } catch (e: StackOverflowError) {
                        throw IonSchemaException(
                            """
                            IonSchemaSystem encountered a circular import graph that cannot be resolved because transitive imports are enabled.
                            You can resolve this in one of the following ways (listed in order of preference):
                              1) Upgrade your schemas to use Ion Schema 2.0.
                              2) Disable transitive imports using IonSchemaSystemBuilder.allowTransitiveImports(false)
                              3) Update your schemas to import individual types rather than importing whole schemas (e.g. `{ id: "my_schema.isl", type: my_type }` instead of `{ id: "my_schema.isl" }`). 
                            """.trimIndent()
                        )
                    }
                }

                val schemaAndTypes = importsMap.getOrPut(childImportId) {
                    SchemaAndTypeImports(childImportId, importedSchemaFn)
                }

                val typeField = it["type"] as? IonSymbol
                val typeName = typeField?.stringValue()
                if (typeName != null) {
                    var importedType: ImportedType? = if (schemaSystem.doesSchemaDeclareType(childImportId, typeField)) {
                        referenceManager.createDeferredImportReference(childImportId, typeField)
                    } else {
                        null
                    }

                    if (importedType == null && allowTransitiveImports) {
                        importedType = importedSchemaFn().getType(typeName)
                            ?.toImportedType(childImportId)
                            ?.also { type ->
                                schemaSystem.emitWarning {
                                    warnInvalidTransitiveImport(type, this.schemaId)
                                }
                            }
                    }

                    importedType ?: throw InvalidSchemaException("Schema $childImportId doesn't contain a type named '$typeName'")

                    if (alias != null) {
                        importedType = TypeAliased(alias, importedType)
                    }
                    addType(typeMap, importedType)
                    schemaAndTypes.addType(alias?.stringValue() ?: typeName, importedType)
                } else {
                    val typesToAdd =
                        if (allowTransitiveImports)
                            importedSchemaFn().getTypes().asSequence().toList()
                        else
                            schemaSystem.listDeclaredTypes(childImportId).map { referenceManager.createDeferredImportReference(childImportId, it) }

                    typesToAdd.map { type -> type.toImportedType(childImportId) }
                        .forEach { type ->
                            addType(typeMap, type)
                            schemaAndTypes.addType(type.name, type)
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

    private fun addType(typeMap: MutableMap<String, TypeInternal>, type: TypeInternal) {
        if (type !is ImportedType) validateType(type)

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

    override fun getInScopeType(name: String) = (schemaCore.getType(name) ?: types[name])

    override fun getDeclaredType(name: String) = declaredTypes[name]

    override fun getDeclaredTypes(): Iterator<TypeInternal> = declaredTypes.values.iterator()

    override fun getTypes(): Iterator<TypeInternal> =
        (schemaCore.getTypes().asSequence() + types.values.asSequence())
            .filter { it is ImportedType || it is TypeImpl }
            .iterator()

    override fun newType(isl: String) = newType(
        schemaSystem.ionSystem.singleValue(isl) as IonStruct
    )

    override fun newType(isl: IonStruct): Type {
        return schemaSystem.usingReferenceManager { TypeImpl(isl, this, it) }
    }

    override fun plusType(type: Type): Schema {
        validateType(type)

        // prepare ISL corresponding to the new Schema
        // (might be simpler if IonDatagram.set(int, IonValue) were implemented,
        // see https://github.com/amazon-ion/ion-java/issues/50)
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
        if (!newTypeAdded) newIsl.add(type.isl.clone())

        return schemaSystem.newSchema(newIsl.iterator())
    }

    override fun getSchemaSystem() = schemaSystem

    /**
     * Returns a new [ImportedType] instance that decorates [Type] so that it will
     * log a transitive import warning every time it is used for validation.
     */
    private fun TypeInternal.toImportedType(importedFromSchemaId: String): ImportedType {
        if (this is ImportedType && !schemaSystem.getParam(IonSchemaSystemImpl.Param.ALLOW_TRANSITIVE_IMPORTS)) return this

        return object : ImportedType, TypeInternal by this {
            override fun validate(value: IonValue, issues: Violations) {
                if (importedFromSchemaId != schemaId) {
                    schemaSystem.emitWarning {
                        warnInvalidTransitiveImport(this, this@SchemaImpl_1_0.schemaId)
                    }
                }
                this@toImportedType.validate(value, issues)
            }

            override val schemaId: String
                get() = this@toImportedType.schemaId!!
            override val importedFromSchemaId: String
                get() = importedFromSchemaId
        }
    }
}
