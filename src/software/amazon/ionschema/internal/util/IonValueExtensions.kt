package software.amazon.ionschema.internal.util

import software.amazon.ion.IonValue

internal fun IonValue.withoutAnnotations(): IonValue {
    if (this.typeAnnotations.size == 0) {
        return this
    }
    val thisWithoutAnnotations = this.clone()
    thisWithoutAnnotations.clearTypeAnnotations()
    return thisWithoutAnnotations
}
