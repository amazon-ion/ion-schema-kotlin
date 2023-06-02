package com.amazon.ionschema.model

import com.amazon.ion.IonNumber
import com.amazon.ion.IonValue
import com.amazon.ion.Timestamp
import com.amazon.ionschema.util.Bag

/**
 * Convenience alias for a collections of open content fields in schema headers, footers, and type definitions.
 * It is modeled as a [Bag] rather than a [List] because ordering is not guaranteed for structs, and we want to be able to
 * compare two collections of open content fields for equality. It is modeled as a [Bag] rather than a [Set] because we
 * don't know if a repeated field name and value has any meaning to the author of the schema, so we can't safely
 * deduplicate them as we can with constraints.
 */
typealias OpenContentFields = Bag<Pair<String, IonValue>>

/**
 * Convenience alias for a set of [TypeArgument].
 */
@ExperimentalIonSchemaModel
typealias TypeArguments = Set<TypeArgument>

/**
 * A [ContinuousRange] of [Timestamp], represented as a [ConsistentTimestamp].
 */
typealias TimestampRange = ContinuousRange<ConsistentTimestamp>

/**
 * A [ContinuousRange] of [IonNumber], represented as [ConsistentDecimal]
 */
typealias NumberRange = ContinuousRange<ConsistentDecimal>

/**
 * A [ContinuousRange] of [TimestampPrecisionValue].
 * `TimestampPrecision` is a discrete measurement (i.e. there is no fractional number of digits of precision).
 * However, because Ion Schema models timestamp precision as an enum, there are possible precisions that exist between
 * the available enum values. For example, `timestamp_precision: range::[exclusive::second, exclusive::millisecond]`
 * allows 1 or 2 digits of precision for the fractional seconds of a timestamp.
 */
typealias TimestampPrecisionRange = ContinuousRange<TimestampPrecisionValue>
