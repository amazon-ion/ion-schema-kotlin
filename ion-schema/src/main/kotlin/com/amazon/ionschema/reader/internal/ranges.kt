package com.amazon.ionschema.reader.internal

import com.amazon.ion.IonInt
import com.amazon.ion.IonList
import com.amazon.ion.IonNumber
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.internal.util.islRequire
import com.amazon.ionschema.internal.util.islRequireExactAnnotations
import com.amazon.ionschema.internal.util.islRequireIonNotNull
import com.amazon.ionschema.internal.util.islRequireIonTypeNotNull
import com.amazon.ionschema.internal.util.islRequireNoIllegalAnnotations
import com.amazon.ionschema.model.ConsistentDecimal
import com.amazon.ionschema.model.ConsistentTimestamp
import com.amazon.ionschema.model.ContinuousRange
import com.amazon.ionschema.model.ContinuousRange.Limit
import com.amazon.ionschema.model.DiscreteIntRange
import com.amazon.ionschema.model.TimestampPrecisionRange
import com.amazon.ionschema.model.TimestampPrecisionValue
import com.amazon.ionschema.model.ValidValue.NumberRange
import com.amazon.ionschema.model.ValidValue.TimestampRange

/**
 * Converts an [IonValue] to a [TimestampRange].
 */
internal fun IonValue.toTimestampRange(): TimestampRange = readRangeBoundaries(ConsistentTimestamp::fromIonTimestamp)
    .let { (start, end) -> TimestampRange(start, end) }

/**
 * Converts an [IonValue] to a [NumberRange].
 */
internal fun IonValue.toNumberRange(): NumberRange = readRangeBoundaries { it: IonNumber ->
    islRequire(it.isNumericValue) { "Invalid number range; range bounds must be real numbers: $this" }
    ConsistentDecimal.fromIonNumber(it)
}.let {
    (start, end) ->
    NumberRange(start, end)
}

/**
 * Converts an [IonValue] to a [TimestampPrecisionRange]
 */
internal fun IonValue.toTimestampPrecisionRange(): TimestampPrecisionRange {
    return when (this) {
        is IonList -> readRangeBoundaries { sym: IonSymbol ->
            TimestampPrecisionValue.fromSymbolTextOrNull(sym.stringValue())
                ?: throw InvalidSchemaException(
                    "Invalid timestamp precision range; range bounds must be ${
                    TimestampPrecisionValue.valueSymbolTexts().joinToString()
                    }, min, or max: $this"
                )
        }.let { (start, end) -> TimestampPrecisionRange(start, end) }

        is IonSymbol -> {
            islRequireIonNotNull(this) { "Timestamp precision value cannot be null; was: $this" }
            islRequireNoIllegalAnnotations(this) { "Timestamp precision value may not have annotations" }
            val precision = TimestampPrecisionValue.fromSymbolTextOrNull(stringValue())
                ?: throw InvalidSchemaException("Invalid timestamp precision range; range bounds must be ${TimestampPrecisionValue.valueSymbolTexts().joinToString() }, min, or max: $this")
            TimestampPrecisionRange(precision)
        }
        else -> throw InvalidSchemaException("Invalid range; not an ion list: $this")
    }
}

/**
 * Converts an [IonValue] to a [ContinuousRange] using the given [valueFn].
 */
private inline fun <T : Comparable<T>, reified IV : IonValue> IonValue.readRangeBoundaries(valueFn: (IV) -> T): Pair<Limit<T>, Limit<T>> {
    return when (this) {
        is IonList -> {
            islRequire(size == 2) { "Invalid range; size of list must be 2:  $this" }
            islRequireExactAnnotations(this, "range") { "Invalid range; missing 'range' annotation:  $this" }
            val lower = readContinuousRangeBoundary(BoundaryPosition.Lower, valueFn)
            val upper = readContinuousRangeBoundary(BoundaryPosition.Upper, valueFn)
            return lower to upper
        }
        else -> throw InvalidSchemaException("Invalid range; not an ion list: $this")
    }
}

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
            DiscreteIntRange(lower, upper)
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
 * Reads and validates a single endpoint of a continuous value range.
 */
private inline fun <T : Comparable<T>, reified IV : IonValue> IonList.readContinuousRangeBoundary(boundaryPosition: BoundaryPosition, valueFn: (IV) -> T): Limit<T> {
    val b = get(boundaryPosition.idx) ?: throw InvalidSchemaException("Invalid range; missing $boundaryPosition boundary value: $this")
    return if (b is IonSymbol && b.stringValue() == boundaryPosition.symbol) {
        islRequire(b.typeAnnotations.isEmpty()) { "Invalid range; min/max may not be annotated: $this" }
        Limit.Unbounded
    } else {
        val value = islRequireIonTypeNotNull<IV>(b) { "Invalid range; $boundaryPosition boundary of range must be '${boundaryPosition.symbol}' or a non-null ${IV::class.simpleName}" }.let(valueFn)
        val exclusive = readBoundaryExclusivity(boundaryPosition)
        if (exclusive) {
            Limit.Open(value)
        } else {
            Limit.Closed(value)
        }
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
