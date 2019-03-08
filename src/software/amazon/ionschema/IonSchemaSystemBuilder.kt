package software.amazon.ionschema

import software.amazon.ion.IonSystem
import software.amazon.ion.system.IonSystemBuilder
import software.amazon.ionschema.internal.ConstraintFactoryDefault
import software.amazon.ionschema.internal.IonSchemaSystemImpl

/**
 * Entry point for Ion Schema.  Provides a builder API for constructing
 * [IonSchemaSystem]s using the specified [Authority]s and IonSystem.
 */
class IonSchemaSystemBuilder private constructor() {
    companion object {
        /**
         * Provides a standard instance of IonSchemaSystemBuilder.
         */
        @JvmStatic
        fun standard() = IonSchemaSystemBuilder()

        private val defaultConstraintFactory = ConstraintFactoryDefault()
    }

    private var authorities = mutableListOf<Authority>()
    private var constraintFactory = defaultConstraintFactory
    private var ionSystem = IonSystemBuilder.standard().build()

    /**
     * Adds the provided authority to the list of [Authority]s.
     */
    fun addAuthority(authority: Authority): IonSchemaSystemBuilder {
        authorities.add(authority)
        return this
    }

    /**
     * Replaces the list of [Authority]s with a list containing only
     * the specified authority.
     */
    fun withAuthority(authority: Authority): IonSchemaSystemBuilder {
        this.authorities = mutableListOf(authority)
        return this
    }

    /**
     * Replaces the list of [Authority]s with the specified list of [Authority]s.
     */
    fun withAuthorities(authorities: List<Authority>): IonSchemaSystemBuilder {
        this.authorities = mutableListOf<Authority>().apply { addAll(authorities) }
        return this
    }

    /**
     * Provides the IonSystem to use when building an IonSchemaSystem.
     */
    fun withIonSystem(ionSystem: IonSystem): IonSchemaSystemBuilder {
        this.ionSystem = ionSystem
        return this
    }

    /**
     * Instantiates an [IonSchemaSystem] using the provided [Authority](s)
     * and IonSystem.
     */
    fun build(): IonSchemaSystem = IonSchemaSystemImpl(
            ionSystem,
            authorities,
            constraintFactory
    )
}

