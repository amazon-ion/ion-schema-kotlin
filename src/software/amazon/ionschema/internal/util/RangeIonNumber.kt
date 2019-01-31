package software.amazon.ionschema.internal.util

import software.amazon.ion.*
import java.math.BigDecimal

internal class RangeIonNumber private constructor (
        private val ion: IonList,
        private val delegate: RangeBigDecimal
) : Range<IonValue> {

    constructor (ion: IonList) : this(ion, RangeBigDecimal(ion))

    companion object {
        private fun toBigDecimal(ion: IonValue) =
                when (ion) {
                    is IonDecimal -> ion.bigDecimalValue()
                    is IonFloat -> ion.bigDecimalValue()
                    is IonInt -> BigDecimal(ion.bigIntegerValue())
                    else -> null
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

