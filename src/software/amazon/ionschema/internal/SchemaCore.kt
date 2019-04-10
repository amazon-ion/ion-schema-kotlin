package software.amazon.ionschema.internal

import software.amazon.ion.*
import software.amazon.ionschema.IonSchemaSystem
import software.amazon.ionschema.Schema
import software.amazon.ionschema.Type

/**
 * Provides instances of [Type] for all of the Core Types and Ion Types
 * defined by the Ion Schema Specification.
 */
internal class SchemaCore(
        private val schemaSystem: IonSchemaSystem
) : Schema {

    private val typeMap: Map<String, Type>

    init {
        val ION = (schemaSystem as IonSchemaSystemImpl).getIonSystem()
        typeMap = ION.iterate(CORE_TYPES + ION_TYPES)
            .asSequence()
            .map { (it as IonStruct).first() as IonSymbol }
            .associateBy({ it.stringValue() }, { newType(it) })
            .toMutableMap()

        ION.iterate(ADDITIONAL_TYPE_DEFS)
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

    override fun getTypes() = typeMap.values.iterator()

    override fun getSchemaSystem() = schemaSystem

    override fun newType(isl: String) = throw UnsupportedOperationException()
    override fun newType(isl: IonStruct) = throw UnsupportedOperationException()
    override fun plusType(type: Type) = throw UnsupportedOperationException()
}

private const val CORE_TYPES = """
        { type: blob }
        { type: bool }
        { type: clob }
        { type: decimal }
        { type: document }
        { type: float }
        { type: int }
        { type: string }
        { type: symbol }
        { type: timestamp }
        { type: list }
        { type: sexp }
        { type: struct }
    """

private const val ION_TYPES = """
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

private const val ADDITIONAL_TYPE_DEFS = """
        { lob:    type::{ one_of: [ blob, clob ] } }

        { number: type::{ one_of: [ decimal, float, int ] } }

        { text:   type::{ one_of: [ string, symbol ] } }

        { any:    type::{ one_of: [ blob, bool, clob, decimal, document,
                                    float, int, string, symbol, timestamp,
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
                                            document,
                                          ] } }

        { nothing:        type::{ not: ${'$'}any } }
    """

