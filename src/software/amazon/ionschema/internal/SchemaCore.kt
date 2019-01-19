package software.amazon.ionschema.internal

import software.amazon.ion.*
import software.amazon.ionschema.IonSchemaSystem
import software.amazon.ionschema.Schema
import software.amazon.ionschema.Type

internal class SchemaCore(
        private val schemaSystem: IonSchemaSystem
    ) : Schema {

    companion object {
        private val CORE_TYPES = """
            { type: blob }
            { type: bool }
            { type: clob }
            { type: decimal }
            { type: float }
            { type: int }
            { type: string }
            { type: symbol }
            { type: timestamp }
            { type: list }
            { type: sexp }
            { type: struct }
        """

        private val ION_TYPES = """
            { type: ${'$'}blob }
            { type: ${'$'}bool }
            { type: ${'$'}clob }
            { type: ${'$'}decimal }
            { type: ${'$'}float }
            { type: ${'$'}int }
            { type: ${'$'}null }
            { type: ${'$'}string }
            { type: ${'$'}symbol }
            { type: ${'$'}timestamp }
            { type: ${'$'}list }
            { type: ${'$'}sexp }
            { type: ${'$'}struct }
        """

        private val ADDITIONAL_TYPE_DEFS = """
            { lob:    type::{ one_of: [ blob, clob ] } }

            { number: type::{ one_of: [ decimal, float, int ] } }

            { text:   type::{ one_of: [ string, symbol ] } }

            { any:    type::{ one_of: [ blob, bool, clob, decimal, float,
                                        int, string, symbol, timestamp,
                                        list, sexp, struct ] } }

            { '${'$'}lob':    type::{ one_of: [ '${'$'}blob', '${'$'}clob' ] } }

            { '${'$'}number': type::{ one_of: [ '${'$'}decimal', '${'$'}float', '${'$'}int' ] } }

            { '${'$'}text':   type::{ one_of: [ '${'$'}string', '${'$'}symbol' ] } }

            { '${'$'}any':    type::{ one_of: [ '${'$'}blob',
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
                                              ] } }

            { nothing:        type::{ not: ${'$'}any } }
        """
    }

    private val typeMap: Map<String, Type>

    init {
        typeMap = schemaSystem.getIonSystem().iterate(CORE_TYPES + ION_TYPES)
            .asSequence()
            .map { (it as IonStruct).first() as IonSymbol }
            .associateBy({ it.stringValue() }, { newType(it) })
            .toMutableMap()

        schemaSystem.getIonSystem().iterate(ADDITIONAL_TYPE_DEFS)
            .asSequence()
            .map { (it as IonStruct).first() }
            .forEach {
                typeMap.put(it.fieldName, TypeBuiltinImpl(it as IonStruct, this))
            }
    }

    private fun newType(name: IonSymbol): Type =
        if (name.stringValue().startsWith("\$")) {
            TypeIon(name)
        } else {
            TypeCore(name)
        }

    override fun getType(name: String): Type? = typeMap[name]

    override fun getType(name: IonSymbol): Type? = getType(name.stringValue())

    override fun getTypes() = typeMap.values.iterator()

    override fun getSchemaSystem() = schemaSystem
}
