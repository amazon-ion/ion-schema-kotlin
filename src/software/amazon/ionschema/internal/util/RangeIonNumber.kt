package software.amazon.ionschema.internal.util

import software.amazon.ion.*
import java.math.BigDecimal

/**
 * Implementation of Range<IonValue> restricted to IonDecimal, IonFloat,
 * and IonInt (numeric) values.  Mostly delegates to RangeBigDecimal.
 */
internal class RangeIonNumber private constructor (
        private val ion: IonList,
        private val delegate: RangeBigDecimal
) : Range<IonValue> {

    constructor (ion: IonList) : this(ion, RangeBigDecimal(ion))

    companion object {
        private fun toBigDecimal(ion: IonValue) =
                if (ion.isNullValue) {
                    null
                } else {
                    when (ion) {
                        is IonDecimal -> ion.bigDecimalValue()
                        is IonFloat -> ion.bigDecimalValue()
                        is IonInt -> BigDecimal(ion.bigIntegerValue())
                        else -> null
                    }
                }
    }

    override fun contains(value: IonValue): Boolean {
        val bdValue = toBigDecimal(value)
        return if (bdValue != null) {
                delegate.contains(bdValue)
            } else {
                false
            }
    }
}

