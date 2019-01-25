package software.amazon.ionschema

import software.amazon.ion.IonStruct
import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.util.truncate

/**
 * Provides methods to create Violations that are common across multiple
 * constraints.
 */
class CommonViolations private constructor() {
    companion object {
        @JvmStatic
        fun INVALID_TYPE(constraint: IonValue, value: IonValue) = Violation(
                constraint,
                "invalid_type",
                "not applicable for type %s".format(value.type.toString().toLowerCase())
        )

        @JvmStatic
        fun NULL_VALUE(constraint: IonValue) = Violation(
                constraint,
                "null_value",
                "not applicable for null values"
        )
    }
}

/**
 * Indicates whether a value validated successfully against a [Type],
 * and if not, provides details indicating why not.  Instances of
 * this class contain zero or more Violation objects, and zero or
 * more ViolationChild objects.  Violation objects indicate constraints
 * that failed at the current level of the value, whereas ViolationChild
 * objects correspond to nesting of Violations within the value's hierarchy.
 */
open class Violations internal constructor (
        private var shortCircuit: Boolean = false,
        private val childrenAllowed: Boolean = true,
        val violations: MutableList<Violation> = mutableListOf()
) : Iterable<Violation> by violations {

    val children: MutableList<ViolationChild> = mutableListOf()

    fun isValid() = violations.isEmpty() && children.isEmpty()

    internal fun add(violation: Violation): Boolean {
        (violation as Violations).shortCircuit = shortCircuit
        violations.add(violation)
        if (shortCircuit) throw ShortCircuitValidationException()
        return true
    }

    internal fun add(child: ViolationChild) {
        if (!childrenAllowed) {
            throw IllegalArgumentException("Children cannot be added to this object")
        }
        (child as Violations).shortCircuit = shortCircuit
        children.add(child)
        if (shortCircuit) throw ShortCircuitValidationException()
    }

    internal inner class Checkpoint(
            private val violationCount: Int,
            private val childCount: Int
    ) {
        fun isValid() = this@Violations.violations.size == violationCount
                        && this@Violations.children.size == childCount
    }

    internal fun checkpoint() = Checkpoint(violations.size, children.size)

    /**
     * Provides a user-friendly string representation of [Violation]s,
     * if there are any;  otherwise returns an empty string.
     */
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
            it.fieldName?.let { sb.append(it) }
            it.index?.let { sb.append("[").append(it).append("]") }
            it.value?.let { sb.append(": ").append(it.toString().truncate(20)) }
            sb.appendln()
            (it as Violations).appendTo(sb, depth + 1)
        }
    }
}

/**
 * Describes a constraint violation, including a violation code and message.
 */
class Violation(
        var constraint: IonValue? = null,
        var code: String? = null,
        var message: String? = null
) : Violations()

/**
 * References a specific struct fieldName or index into a list/sexp/document
 * within a hierarchical Violations object.
 */
class ViolationChild internal constructor (
        val fieldName: String? = null,
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

/**
 * This exception is only thrown to indicate that validation processing
 * can be short-circuited (specifically in cases where [Type.isValid()] was
 * invoked, and at least one constraint has been violated).
 */
internal class ShortCircuitValidationException : Exception()

