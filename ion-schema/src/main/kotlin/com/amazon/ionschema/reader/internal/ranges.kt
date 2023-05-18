package com.amazon.ionschema.reader.internal

import com.amazon.ion.IonInt
import com.amazon.ion.IonList
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.internal.util.islRequire
import com.amazon.ionschema.internal.util.islRequireExactAnnotations
import com.amazon.ionschema.internal.util.islRequireIonNotNull
import com.amazon.ionschema.internal.util.islRequireIonTypeNotNull
import com.amazon.ionschema.internal.util.islRequireNoIllegalAnnotations
import com.amazon.ionschema.model.DiscreteIntRange

/**
 * Converts an [IonValue] to a [DiscreteIntRange]
 */
internal fun IonValue.toDiscreteIntRange(): DiscreteIntRange {
    return when (this) {
        is IonList -> {
            islRequire(size == 2) { "Invalid range; size of list must be 2:  $this" }
            islRequireExactAnnotations(this, "range") { "Invalid range; missing 'range' annotation:  $this" }
            val lower = readDiscreteIntRangeBoundary(BoundaryPosition.Lower)
            val upper = readDiscreteIntRangeBoundary(BoundaryPosition.Upper)
            return DiscreteIntRange(lower, upper)
        }
        is IonInt -> {
            islRequireIonNotNull(this) { "Range cannot be a null value" }
            islRequireNoIllegalAnnotations(this) { "Constraint may not be annotated" }
            DiscreteIntRange(this.intValue(), this.intValue())
        }
        else -> throw InvalidSchemaException("Invalid range; not an ion list: $this")
    }
}

/**
 * Represents either the upper bound or the lower bound. Allows us to write functions that can be re-used for both
 * bounds to avoid duplicated code.
 */
internal enum class BoundaryPosition(val idx: Int, val symbol: String, val intExclusivityAdjustment: Int) {
    Lower(0, "min", intExclusivityAdjustment = 1),
    Upper(1, "max", intExclusivityAdjustment = -1);
}

/**
 * Reads and validates a single endpoint of an int range.
 */
private fun IonList.readDiscreteIntRangeBoundary(bp: BoundaryPosition): Int? {
    val it = get(bp.idx) ?: throw InvalidSchemaException("Invalid range; missing $bp boundary value: $this")
    return if (it is IonSymbol && it.stringValue() == bp.symbol) {
        islRequireNoIllegalAnnotations(it) { "Invalid range; '${bp.symbol}' may not be exclusive: $it" }
        null
    } else {
        val value = islRequireIonTypeNotNull<IonInt>(it) { "Invalid range; $bp boundary of range must be '${bp.symbol}' or a non-null int" }
        value.intValue() + if (readBoundaryExclusivity(bp)) bp.intExclusivityAdjustment else 0
    }
}

/**
 * Reads and validates annotations on the endpoint of a range, returning true iff the endpoint is exclusive.
 */
private fun IonList.readBoundaryExclusivity(boundaryPosition: BoundaryPosition): Boolean {
    val b = get(boundaryPosition.idx)!!
    islRequireNoIllegalAnnotations(b, "exclusive") { "Invalid range; illegal annotation on $boundaryPosition bound: $this" }
    return b.hasTypeAnnotation("exclusive")
}
