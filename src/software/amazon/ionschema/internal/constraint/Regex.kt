package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonString
import software.amazon.ion.IonText
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.internal.util.Violations
import software.amazon.ionschema.internal.util.Violation
import software.amazon.ionschema.internal.util.CommonViolations
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

    override fun validate(value: IonValue, issues: Violations) {
        if (value !is IonText) {
            issues.add(CommonViolations.INVALID_TYPE(ion, value))
        } else if (value.isNullValue) {
            issues.add(CommonViolations.NULL_VALUE(ion))
        } else {
            val string = value.stringValue().replace("\"", "\\\"")
            val expr = "(/$regex/$flags).test(\"" + string + "\")"
            val result = scriptEngine.eval(expr) as Boolean

            if (!result) {
                issues.add(Violation(ion, "regex_mismatch", "value doesn't match regex"))
            }
        }
    }
}
