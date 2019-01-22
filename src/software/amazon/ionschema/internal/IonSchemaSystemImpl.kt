package software.amazon.ionschema.internal

import software.amazon.ion.IonSystem
import software.amazon.ion.IonValue
import software.amazon.ionschema.*
import java.io.Reader

internal class IonSchemaSystemImpl(
        private val ionSystem: IonSystem,
        private val authorities: List<Authority>
    ) : IonSchemaSystem {

    private val ION = ionSystem
    private val constraintFactory = ConstraintFactoryDefault()
    private val schemaCore = SchemaCore(this)
    private val schemaCache = mutableMapOf<String, Schema>()

    override fun loadSchema(id: String): Schema {
        var cachedSchema = schemaCache.get(id)
        if (cachedSchema != null) {
            return cachedSchema
        }

        var exceptions = mutableListOf<Exception>()
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

        throw IonSchemaException("Unable to resolve schema id '$id' ($exceptions)")
    }

    override fun loadSchema(reader: Reader): Schema {
        val iterator = ION.iterate(reader)
        return SchemaImpl(this, schemaCore, iterator)
    }

    internal fun isConstraint(name: String)
            = constraintFactory.isConstraint(name)

    internal fun constraintFor(ion: IonValue, schema: Schema, type: Type?)
            = constraintFactory.constraintFor(ion, schema, type)

    override fun getIonSystem() = ION
}
