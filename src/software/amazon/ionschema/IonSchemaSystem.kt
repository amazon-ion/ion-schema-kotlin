package software.amazon.ionschema

import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.*
import java.io.Reader

class IonSchemaSystem private constructor (
        private val authorities: List<Authority>,
        private val constraintFactory: ConstraintFactory
    ) {

    class Builder private constructor() {
        companion object {
            @JvmStatic
            fun standard() = Builder()

            private val defaultConstraintFactory: ConstraintFactory = ConstraintFactoryDefault()
        }

        private var authorities = mutableListOf<Authority>()
        private var constraintFactory = defaultConstraintFactory

        fun addAuthority(authority: Authority): Builder {
            authorities.add(authority)
            return this
        }

        fun withAuthority(authority: Authority): Builder {
            this.authorities = mutableListOf(authority)
            return this
        }

        fun withAuthorities(authorities: MutableList<Authority>): Builder {
            this.authorities = authorities  // TBD clone
            return this
        }

        /*
        fun withConstraintFactory(constraintFactory: ConstraintFactory): Builder {
            this.constraintFactory = constraintFactory
            return this
        }
        */

        fun build() = IonSchemaSystem(authorities, constraintFactory)
    }

    private val schemaCore = SchemaCore(this)
    private val schemaCache = mutableMapOf<String,Schema>()

    fun loadSchema(id: String): Schema {
        var cachedSchema = schemaCache.get(id)
        if (cachedSchema != null) {
            return cachedSchema
        }

        var exceptions = mutableListOf<Exception>()
        authorities.forEach { authority ->
            try {
                val reader = authority.readerFor(id)
                reader?.use {
                    val iterator = ION.iterate(reader)
                    val schema = SchemaImpl(this, schemaCore, iterator)
                    schemaCache.put(id, schema)
                    return schema
                }
            } catch (e: Exception) {
                exceptions.add(e)
            }
        }

        throw IonSchemaException("Unable to resolve schema id '$id' ($exceptions)")
    }

    fun loadSchema(reader: Reader): Schema {
        val iterator = ION.iterate(reader)
        return SchemaImpl(this, schemaCore, iterator)
    }

    internal fun isConstraint(name: String)
            = constraintFactory.isConstraint(name)

    internal fun constraintFor(ion: IonValue, schema: Schema, type: Type?)
            = constraintFactory.constraintFor(ion, schema, type)
}
