package software.amazon.ionschema

import software.amazon.ion.IonSystem
import software.amazon.ion.system.IonSystemBuilder
import software.amazon.ionschema.internal.ConstraintFactoryDefault
import software.amazon.ionschema.internal.IonSchemaSystemImpl

class IonSchemaSystemBuilder private constructor() {
    companion object {
        @JvmStatic
        fun standard() = IonSchemaSystemBuilder()

        private val defaultConstraintFactory = ConstraintFactoryDefault()
    }

    private var authorities = mutableListOf<Authority>()
    private var constraintFactory = defaultConstraintFactory
    private var ionSystem = IonSystemBuilder.standard().build()

    fun addAuthority(authority: Authority): IonSchemaSystemBuilder {
        authorities.add(authority)
        return this
    }

    fun withAuthority(authority: Authority): IonSchemaSystemBuilder {
        this.authorities = mutableListOf(authority)
        return this
    }

    fun withAuthorities(authorities: MutableList<Authority>): IonSchemaSystemBuilder {
        this.authorities = mutableListOf<Authority>().apply { addAll(authorities) }
        return this
    }

    fun withIonSystem(ionSystem: IonSystem): IonSchemaSystemBuilder {
        this.ionSystem = ionSystem
        return this
    }

    /*
    fun withConstraintFactory(constraintFactory: ConstraintFactory): Builder {
        this.constraintFactory = constraintFactory
        return this
    }
    */

    fun build(): IonSchemaSystem = IonSchemaSystemImpl(
            ionSystem,
            authorities,
            constraintFactory
    )
}

