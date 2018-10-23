package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonString
import software.amazon.ion.IonText
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException
import javax.script.ScriptEngineManager

internal class Regex(
        ion: IonValue
    ) : ConstraintBase(ion) {

    companion object {
        private val scriptEngine = ScriptEngineManager().getEngineByName("javascript")
    }

    private val regex = (ion as IonString).stringValue()
    private val flags: String

    init {
        val sb = StringBuffer()
        ion.typeAnnotations.forEach {
            when (it) {
                "i", "m" -> sb.append(it)
                else -> throw InvalidSchemaException(
                        "Unrecognized flags for regex ($ion)")
            }
        }
        flags = sb.toString()
    }

    override fun isValid(value: IonValue): Boolean {
        if (value is IonText && !value.isNullValue) {
            val string = value.stringValue().replace("\"", "\\\"")
            val expr = "(/$regex/$flags).test(\"" + string + "\")"
            return scriptEngine.eval(expr) as Boolean
        }
        return false
    }
}
