package software.amazon.ionschema.internal.util

import software.amazon.ion.IonStruct
import software.amazon.ion.IonValue
import software.amazon.ionschema.Type
import software.amazon.ionschema.internal.TypeInternal
import java.util.Collections.addAll

internal class Validator {
    companion object {
        @JvmStatic
        internal fun isValid(type: Type, value: IonValue) = validate(type, value, true).isValid()

        @JvmStatic
        internal fun validate(type: Type, value: IonValue) = validate(type, value, false)

        private fun validate(type: Type, value: IonValue, shortCircuit: Boolean): Violations {
            val violations = Violations(
                    shortCircuit = shortCircuit,
                    childrenAllowed = false)
            try {
                (type as TypeInternal).validate(value, violations)
            } catch (e: ShortCircuitValidationException) {
                // short-circuit validation winds up here, safe to ignore
            }
            return violations
        }
    }
}

class CommonViolations private constructor() {
    companion object {
        fun INVALID_TYPE(constraint: IonValue, value: IonValue) = Violation(
                constraint,
                "invalid_type",
                "not applicable for type %s".format(value.type.toString().toLowerCase())
        )

        fun NULL_VALUE(constraint: IonValue) = Violation(
                constraint,
                "null_value",
                "not applicable for null values"
        )
    }
}

open class Violations (
        private var shortCircuit: Boolean = false,
        private val childrenAllowed: Boolean = true,
        val violations: MutableList<Violation> = mutableListOf()
) : List<Violation> by violations {

    val children: MutableList<ViolationChild> = mutableListOf()

    fun isValid() = violations.isEmpty() && children.isEmpty()

    fun add(violation: Violation): Boolean {
        (violation as Violations).shortCircuit = shortCircuit
        violations.add(violation)
        //if (shortCircuit) throw ShortCircuitValidationException()
        return true
    }

    fun add(child: ViolationChild) {
        if (!childrenAllowed) {
            throw IllegalArgumentException("Children cannot be added to this object")
        }
        (child as Violations).shortCircuit = shortCircuit
        children.add(child)
        //if (shortCircuit) throw ShortCircuitValidationException()
    }

    internal inner class Checkpoint(
            val violationCount: Int,
            val childCount: Int
    ) {
        fun isValid() = this@Violations.violations.size == violationCount
                        && this@Violations.children.size == childCount
    }

    internal fun checkpoint() = Checkpoint(violations.size, children.size)

    override fun toString(): String {
        if (violations.size > 0 || children.size > 0) {
            val sb = StringBuilder("Validation failed:").appendln()
            appendTo(sb)
            return sb.toString()
        }
        return ""
    }

    private fun appendTo(sb: StringBuilder, depth: Int = 0) {
        violations.forEach {
            sb.append(" ".repeat(2 * depth))
            sb.append("- ").append(it.message).appendln()
            (it as Violations).appendTo(sb, depth + 1)
        }
        children.forEach {
            sb.append(" ".repeat(2 * depth))
            sb.append("- ")
            it.path?.let { sb.append(it) }
            it.index?.let { sb.append("[").append(it).append("]") }
            it.value?.let { sb.append(": ").append(it.toString().truncate(20)) }
            sb.appendln()
            (it as Violations).appendTo(sb, depth + 1)
        }
    }
}

class Violation(
        var constraint: IonValue? = null,
        var code: String? = null,
        var message: String? = null
) : Violations()


class ViolationChild internal constructor (
        val path: String? = null,
        val index: Int? = null,
        var value: IonValue? = null
) : Violations() {

    internal fun addValue(v: IonValue) {
        if (value == null) {
            value = v
        } else {
            if (v.fieldName != null) {
                if (value !is IonStruct) {
                    val tmp = value
                    value = tmp!!.system.newEmptyStruct()
                    (value as IonStruct).add(tmp.fieldName, tmp.clone())
                }
                (value as IonStruct).add(v.fieldName, v.clone())
            }
        }
    }
}

internal class ShortCircuitValidationException : Exception()
