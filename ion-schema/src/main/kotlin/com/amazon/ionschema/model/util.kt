package com.amazon.ionschema.model

/**
 * Convenience function to easily negate a constraint.
 */
@ExperimentalIonSchemaModel
internal fun not(c: Constraint): Constraint {
    return Constraint.Not(TypeArgument.InlineType(TypeDefinition(setOf(c))))
}

/**
 * Convenience function for creating an inline type argument from a list of constraints.
 */
@ExperimentalIonSchemaModel
internal fun inlineType(constraints: Set<Constraint>): TypeArgument.InlineType {
    return TypeArgument.InlineType(TypeDefinition(constraints))
}
