package software.amazon.ionschema.internal

import software.amazon.ion.IonSystem
import software.amazon.ion.IonValue
import software.amazon.ionschema.Authority
import software.amazon.ionschema.IonSchemaException
import software.amazon.ionschema.IonSchemaSystem
import software.amazon.ionschema.Schema
import software.amazon.ionschema.Type

internal class IonSchemaSystemImpl(
        private val ION: IonSystem,
        private val authorities: List<Authority>,
        private val constraintFactory: ConstraintFactory
) : IonSchemaSystem {

    private val schemaCore = SchemaCore(this)
    private val schemaCache = mutableMapOf<String, Schema>()

    override fun loadSchema(id: String): Schema {
        val cachedSchema = schemaCache.get(id)
        if (cachedSchema != null) {
            return cachedSchema
        }

        val exceptions = mutableListOf<Exception>()
        authorities.forEach { authority ->
            try {
                authority.iteratorFor(this, id).use {
                    if (it.hasNext()) {
                        val schema = SchemaImpl(this, schemaCore, it)
                        schemaCache.put(id, schema)
                        return schema
                    }
                }
            } catch (e: Exception) {
                exceptions.add(e)
            }
        }

        val message = StringBuilder("Unable to resolve schema id '$id'")
        if (exceptions.size > 0) {
            message.append(" ($exceptions)")
        }
        throw IonSchemaException(message.toString())
    }

    override fun newSchema() = newSchema("")

    override fun newSchema(isl: String) = newSchema(ION.iterate(isl))

    override fun newSchema(isl: Iterator<IonValue>) = SchemaImpl(this, schemaCore, isl)

    internal fun isConstraint(name: String)
            = constraintFactory.isConstraint(name)

    internal fun constraintFor(ion: IonValue, schema: Schema, type: Type?)
            = constraintFactory.constraintFor(ion, schema, type)

    internal fun getIonSystem() = ION
}

