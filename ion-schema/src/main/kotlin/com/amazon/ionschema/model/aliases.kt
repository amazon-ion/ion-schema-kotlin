package com.amazon.ionschema.model

import com.amazon.ion.IonNumber
import com.amazon.ion.IonValue
import com.amazon.ion.Timestamp
import java.math.BigDecimal

/**
 * Convenience alias for a collections of open content fields in schema headers, footers, and type definitions.
 */
typealias OpenContentFields = List<Pair<String, IonValue>>

/**
 * Convenience alias for a list of [TypeArgument].
 */
@ExperimentalIonSchemaModel
typealias TypeArgumentList = List<TypeArgument>

/**
 * A [ContinuousRange] of [Timestamp].
 */
typealias TimestampRange = ContinuousRange<Timestamp>

/**
 * A [ContinuousRange] of [IonNumber], represented as [BigDecimal]
 */
typealias NumberRange = ContinuousRange<BigDecimal>

/**
 * A [ContinuousRange] of [TimestampPrecisionValue].
 * `TimestampPrecision` is a discrete measurement (i.e. there is no fractional number of digits of precision).
 * However, because Ion Schema models timestamp precision as an enum, there are possible precisions that exist between
 * the available enum values. For example, `timestamp_precision: range::[exclusive::second, exclusive::millisecond]`
 * allows 1 or 2 digits of precision for the fractional seconds of a timestamp.
 */
typealias TimestampPrecisionRange = ContinuousRange<TimestampPrecisionValue>
