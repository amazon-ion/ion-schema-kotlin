package software.amazon.ionschema.internal.util

import software.amazon.ion.IonInt
import software.amazon.ion.IonList
import software.amazon.ion.IonSymbol
import software.amazon.ionschema.InvalidSchemaException

internal class RangeIntNonNegative (
        private val ion: IonList,
        private val delegate: RangeInt = RangeInt(ion)
) : Range<Int> by delegate {

    init {
        if (!((ion[0] is IonInt && (ion[0] as IonInt).intValue() >= 0)
                        || (ion[0] as? IonSymbol)?.stringValue().equals("min"))) {
            throw InvalidSchemaException("Invalid lower bound in positive int $ion")
        }

        if (!((ion[1] is IonInt && (ion[1] as IonInt).intValue() >= 0)
                        || (ion[1] as? IonSymbol)?.stringValue().equals("max"))) {
            throw InvalidSchemaException("Invalid upper bound in positive int $ion")
        }
    }

    internal fun isAtMax(value: Int) = delegate.isAtMax(value)
}

