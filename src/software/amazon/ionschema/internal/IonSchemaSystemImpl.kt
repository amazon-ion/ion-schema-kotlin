package software.amazon.ionschema.internal

import software.amazon.ion.IonSystem
import software.amazon.ion.IonValue
import software.amazon.ionschema.Authority
import software.amazon.ionschema.IonSchemaException
import software.amazon.ionschema.IonSchemaSystem
import software.amazon.ionschema.Schema

/**
 * Implementation of [IonSchemaSystem].
 */
internal class IonSchemaSystemImpl(
        private val ION: IonSystem,
        private val authorities: List<Authority>,
        private val constraintFactory: ConstraintFactory
) : IonSchemaSystem {

    private val schemaCore = SchemaCore(this)
    private val schemaCache = mutableMapOf<String, Schema>()

    override fun loadSchema(id: String) =
        schemaCache.getOrPut(id, {
            val exceptions = mutableListOf<Exception>()
            authorities.forEach { authority ->
                try {
                    authority.iteratorFor(this, id).use {
                        if (it.hasNext()) {
                            return SchemaImpl(this, schemaCore, it)
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
        })

    override fun newSchema() = newSchema("")

    override fun newSchema(isl: String) = newSchema(ION.iterate(isl))

    override fun newSchema(isl: Iterator<IonValue>) = SchemaImpl(this, schemaCore, isl)

    internal fun isConstraint(name: String)
            = constraintFactory.isConstraint(name)

    internal fun constraintFor(ion: IonValue, schema: Schema)
            = constraintFactory.constraintFor(ion, schema)

    internal fun getIonSystem() = ION
}

