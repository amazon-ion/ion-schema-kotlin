package software.amazon.ionschema.internal

import software.amazon.ion.IonStruct
import software.amazon.ionschema.Schema

// represents built-in types that are defined in terms of other built-in types
internal class TypeBuiltinImpl constructor (
        ionStruct: IonStruct,
        schema: Schema
) : TypeInternal by TypeImpl(ionStruct, schema, addDefaultTypeConstraint = false)
