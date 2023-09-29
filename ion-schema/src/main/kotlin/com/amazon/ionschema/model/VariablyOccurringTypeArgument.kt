package com.amazon.ionschema.model

/**
 * Represents a Type as a constraint argument with additional information about the number of times this type can occur.
 * See [Type Definitions](https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#type-definitions) in ISL 1.0 spec.
 * See [Variably Occurring Type Arguments](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#variably-occurring-type-arguments) in ISL 2.0 spec.
 */
@ExperimentalIonSchemaModel
data class VariablyOccurringTypeArgument(val occurs: DiscreteIntRange, val typeArg: TypeArgument) {
    companion object {
        @JvmStatic
        val OCCURS_OPTIONAL = DiscreteIntRange(0, 1)
        @JvmStatic
        val OCCURS_REQUIRED = DiscreteIntRange(1, 1)

        @JvmStatic
        fun optional(arg: TypeArgument) = VariablyOccurringTypeArgument(OCCURS_OPTIONAL, arg)

        @JvmStatic
        fun required(arg: TypeArgument) = VariablyOccurringTypeArgument(OCCURS_REQUIRED, arg)
    }
}

@ExperimentalIonSchemaModel
fun TypeArgument.optional() = VariablyOccurringTypeArgument.optional(this)

@ExperimentalIonSchemaModel
fun TypeArgument.required() = VariablyOccurringTypeArgument.required(this)

@ExperimentalIonSchemaModel
fun TypeArgument.occurs(n: Int) = VariablyOccurringTypeArgument(DiscreteIntRange(n), this)

@ExperimentalIonSchemaModel
fun TypeArgument.occurs(min: Int?, max: Int?) = VariablyOccurringTypeArgument(DiscreteIntRange(min, max), this)
