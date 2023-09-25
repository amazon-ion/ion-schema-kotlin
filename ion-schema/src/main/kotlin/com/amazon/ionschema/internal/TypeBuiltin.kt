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

import com.amazon.ion.IonType
import com.amazon.ion.IonValue
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.Violation
import com.amazon.ionschema.Violations
import com.amazon.ionschema.internal.util.schemaTypeName

/**
 * Represents a type that is defined in the Ion Schema Specification.
 *
 * All implementations should be as lightweight as possible (e.g. no delegation to other [TypeInternal] instances)
 * because these are the most commonly used types that are used as part of building almost every user-defined type.
 */
internal sealed class TypeBuiltin : TypeInternal

private val ionSystem = IonSystemBuilder.standard().build()

/**
 * The `$any` type gets special treatment because it is a no-op.
 */
private object UniversalType : TypeBuiltin() {
    override val schemaId: String?
        get() = null
    override fun getBaseType(): TypeBuiltin = this
    override fun isValidForBaseType(value: IonValue): Boolean = true
    override val name: String = "\$any"
    override val isl: IonValue = ionSystem.newSymbol("\$any")
    override fun validate(value: IonValue, issues: Violations) = Unit
}

/**
 * The `any` type gets special treatment because it is the default type for ISL 1.0
 */
private object AnyType : TypeBuiltin() {
    override val schemaId: String?
        get() = null
    override fun getBaseType(): TypeBuiltin = this
    override fun isValidForBaseType(value: IonValue): Boolean = true
    override val name: String = "any"
    override val isl: IonValue = ionSystem.newSymbol("any")
    override fun validate(value: IonValue, issues: Violations) {
        if (value.isNullValue) {
            val typeName = value.type.schemaTypeName()
            issues.add(Violation(isl, "type_mismatch", message = "expected type any, found null $typeName"))
        }
    }
}

/**
 * The `nothing` type gets special treatment because all data is invalid.
 */
private object EmptyType : TypeBuiltin() {
    override val schemaId: String?
        get() = null
    override fun getBaseType(): TypeBuiltin = this
    override fun isValidForBaseType(value: IonValue): Boolean = false
    override val name: String = "nothing"
    override val isl: IonValue = ionSystem.newSymbol("nothing")
    override fun validate(value: IonValue, issues: Violations) {
        val typeName = value.type.schemaTypeName()
        issues.add(Violation(isl, "type_mismatch", message = "expected type nothing, found $typeName"))
    }
}

/**
 * An implementation of [TypeBuiltin] that is valid for a set of [IonType]s and optionally the corresponding typed nulls,
 * as defined in the Ion Schema specification.
 */
private class SpecType(override val name: String, private val ionTypes: Set<IonType>, private val nullsAllowed: Boolean) : TypeBuiltin() {
    override val schemaId: String?
        get() = null

    override fun getBaseType(): TypeBuiltin = this

    override fun isValidForBaseType(value: IonValue): Boolean = value.type in ionTypes

    override val isl: IonValue = ionSystem.newSymbol(name)

    override fun validate(value: IonValue, issues: Violations) {
        val typeCheckFailed = value.type !in ionTypes
        val nullCheckFailed = !nullsAllowed and value.isNullValue
        if (typeCheckFailed || nullCheckFailed) {
            val maybeNull = if (value.isNullValue) "null " else ""
            val typeName = value.type.schemaTypeName()
            issues.add(Violation(isl, "type_mismatch", message = "expected type $name, found $maybeNull$typeName"))
        }
    }
}

/**
 * Object for holding all instances of [TypeBuiltin].
 */
internal object BuiltInTypes {
    private val allTypes: MutableMap<String, TypeBuiltin> = mutableMapOf()

    init {
        // Micro DSL so that we can declaratively create the Spec types.
        infix fun String.isNonNull(types: Set<IonType>) {
            allTypes[this] = SpecType(this, types, nullsAllowed = false)
        }
        infix fun String.isMaybeNull(types: Set<IonType>) {
            allTypes[this] = SpecType(this, types, nullsAllowed = true)
        }

        // Creates all the SpecTypes that correspond directly to _one_ IonType.
        IonType.values().forEach {
            val typeName = it.schemaTypeName()
            if (it != IonType.NULL) typeName isNonNull setOf(it)
            if (it != IonType.DATAGRAM) "$$typeName" isMaybeNull setOf(it)
        }

        "text" isNonNull setOf(IonType.STRING, IonType.SYMBOL)
        "\$text" isMaybeNull setOf(IonType.STRING, IonType.SYMBOL)
        "number" isNonNull setOf(IonType.INT, IonType.FLOAT, IonType.DECIMAL)
        "\$number" isMaybeNull setOf(IonType.INT, IonType.FLOAT, IonType.DECIMAL)
        "lob" isNonNull setOf(IonType.BLOB, IonType.CLOB)
        "\$lob" isMaybeNull setOf(IonType.BLOB, IonType.CLOB)
        allTypes["any"] = AnyType
        allTypes["\$any"] = UniversalType
        allTypes["nothing"] = EmptyType
    }

    /** Gets one of the spec types by name */
    operator fun get(name: String): TypeBuiltin? = allTypes[name]

    /** Gets all the spec types as a map */
    fun asMap(): Map<String, TypeBuiltin> = allTypes

    /** Gets all the spec types as a sequence */
    fun asSequence() = allTypes.values.asSequence()
}
