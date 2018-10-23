package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonList
import software.amazon.ion.IonSymbol
import software.amazon.ion.IonValue

internal class Annotations(
        ion: IonValue
    ) : ConstraintBase(ion) {

    private data class Annotation(
            val text: String,
            val isRequired: Boolean
    )

    private val requiredByDefault = ion.hasTypeAnnotation("required")

    private val ordered = ion.hasTypeAnnotation("ordered")

    private val annotations = (ion as IonList).map {
            val required = if (it.hasTypeAnnotation("required")) {
                true
            } else if (it.hasTypeAnnotation("optional")) {
                false
            } else {
                requiredByDefault
            }
            Annotation((it as IonSymbol).stringValue(), required)
        }

    override fun isValid(value: IonValue): Boolean {
        if (ordered) {
            val valueAnnotations = value.typeAnnotations
            var valueAnnotationIndex = 0
            annotations.forEach {
                if (it.isRequired) {
                    var found = false
                    while (!found && valueAnnotationIndex < valueAnnotations.size) {
                        val valueAnnotation = valueAnnotations[valueAnnotationIndex]
                        if (it.text.equals(valueAnnotation)) {
                            found = true
                        }
                    }
                    if (!found) {
                        return false
                    }
                }
            }
        } else {
            annotations.forEach {
                if (it.isRequired && !value.hasTypeAnnotation(it.text)) {
                    return false
                }
            }
        }
        return true
    }
}
