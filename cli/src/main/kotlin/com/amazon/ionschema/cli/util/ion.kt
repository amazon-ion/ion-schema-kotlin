package com.amazon.ionschema.cli.util

import com.amazon.ionelement.api.AnyElement
import com.amazon.ionelement.api.ContainerElement
import com.amazon.ionelement.api.IonElement

sealed class TraversalOrder
internal object PreOrder : TraversalOrder()
internal object PostOrder : TraversalOrder()

/**
 * Visits every element contained in (and including) this [IonElement].
 */
internal fun IonElement.recursivelyVisit(order: TraversalOrder, visitor: (AnyElement) -> Unit) {
    with(this.asAnyElement()) {
        if (order is PreOrder) visitor(this)
        if (this is ContainerElement) asContainerOrNull()?.values?.forEach { child -> child.recursivelyVisit(order, visitor) }
        if (order is PostOrder) visitor(this)
    }
}
