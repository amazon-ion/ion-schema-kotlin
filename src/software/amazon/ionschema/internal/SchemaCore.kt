package software.amazon.ionschema.internal

import software.amazon.ion.*
import software.amazon.ionschema.IonSchemaSystem
import software.amazon.ionschema.Schema
import software.amazon.ionschema.Type
import software.amazon.ionschema.internal.util.withoutAnnotations

internal class SchemaCore(
        private val schemaSystem: IonSchemaSystem
    ) : Schema {

    companion object {
        private val CORE_TYPES = listOf(
                "blob",
                "bool",
                "clob",
                "decimal",
                "float",
                "int",
                "string",
                "symbol",
                "timestamp",
                "list",
                "sexp",
                "struct"
        )

        private val ION_TYPES = listOf(
                "\$blob",
                "\$bool",
                "\$clob",
                "\$decimal",
                "\$float",
                "\$int",
                "\$null",
                "\$string",
                "\$symbol",
                "\$timestamp",
                "\$list",
                "\$sexp",
                "\$struct"
        )

        private val ADDITIONAL_TYPE_DEFS = """
            {
              lob:    type::{ one_of: [ blob, clob ] },

              number: type::{ one_of: [ decimal, float, int ] },

              text:   type::{ one_of: [ string, symbol ] },

              any:    type::{ one_of: [ blob, bool, clob, decimal, float,
                                        int, string, symbol, timestamp,
                                        list, sexp, struct ] },

              '${'$'}lob':    type::{ one_of: [ '${'$'}blob', '${'$'}clob' ] },

              '${'$'}number': type::{ one_of: [ '${'$'}decimal', '${'$'}float', '${'$'}int' ] },

              '${'$'}text':   type::{ one_of: [ '${'$'}string', '${'$'}symbol' ] },

              '${'$'}any':    type::{ one_of: [ '${'$'}blob',
                                                '${'$'}bool',
                                                '${'$'}clob',
                                                '${'$'}decimal',
                                                '${'$'}float',
                                                '${'$'}int',
                                                '${'$'}null',
                                                '${'$'}string',
                                                '${'$'}symbol',
                                                '${'$'}timestamp',
                                                '${'$'}list',
                                                '${'$'}sexp',
                                                '${'$'}struct',
                                              ] },

              nothing:        type::{ not: ${'$'}any },
            }
        """
    }

    private val typeMap: Map<String, Type>

    init {
        typeMap = listOf(CORE_TYPES, ION_TYPES)
            .flatten()
            .asSequence()
            .associateBy({ (schemaSystem.getIonSystem().singleValue("$it") as IonSymbol).stringValue() }, { newType(it) })
            .toMutableMap()

        (schemaSystem.getIonSystem().singleValue(ADDITIONAL_TYPE_DEFS) as IonStruct).forEach {
            typeMap.put(it.fieldName, TypeImpl(it as IonStruct, this, addDefaultTypeConstraint = false))
        }
    }

    private fun newType(name: String) =
        if (name.startsWith("\$")) {
            TypeIon(name)
        } else {
            TypeCore(name)
        }

    override fun getType(name: String): Type? = typeMap.get(name)

    override fun getType(name: IonSymbol): Type? = getType(name.stringValue())

    override fun getTypes() = typeMap.values.iterator()

    override fun getSchemaSystem() = schemaSystem
}
