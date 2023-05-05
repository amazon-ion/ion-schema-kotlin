package com.amazon.ionschema.reader

import com.amazon.ion.IonSequence
import com.amazon.ion.IonStruct
import com.amazon.ion.IonValue

/**
 * Represents a path through a tree of Ion values as a sequence of field names and indices.
 *
 * For example:
 * ```ion
 * type::{
 *   name: foo,
 *   fields: {
 *     a: {
 *       one_of: [
 *         int,
 *         string,  // The path from the top-level value (type::{ ... }) to this value is ('fields' 'a' 'one_of' 1)
 *       ]
 *     }
 *   }
 * }
 * ```
 *
 * Be aware that there is ambiguity with field names because a struct can have more than one field with the same name.
 */
internal data class IonTreePath(val pathElements: List<Element>) : Comparable<IonTreePath> {

    override fun compareTo(other: IonTreePath): Int {
        val thisIter = this.pathElements.iterator()
        val otherIter = other.pathElements.iterator()
        while (thisIter.hasNext() && otherIter.hasNext()) {
            val result = thisIter.next().compareTo(otherIter.next())
            if (result != 0) return result
        }
        if (thisIter.hasNext()) return 1
        if (otherIter.hasNext()) return -1
        return 0
    }

    /**
     * Represents a single element in an [IonTreePath]—either a [Index] or a [Field].
     */
    sealed class Element : Comparable<Element> {
        data class Index(val value: Int) : Element()
        data class Field(val value: String) : Element()

        // Index before Field, otherwise compare the wrapped values
        override fun compareTo(other: Element): Int {
            return when (this) {
                is Index -> if (other is Index) this.value.compareTo(other.value) else -1
                is Field -> if (other is Field) this.value.compareTo(other.value) else if (other is Index) 1 else -1
            }
        }
    }

    companion object {
        /**
         * Factory function for creating an [IonTreePath] for an [IonValue]. This function follows [IonValue.getContainer]
         * building the path until it gets to a value that does not have a parent container.
         */
        internal fun IonValue.treePath(): IonTreePath = IonTreePath(createPathElementList(this))

        private fun createPathElementList(value: IonValue): MutableList<Element> {
            val parent = value.container ?: return mutableListOf()
            val path = createPathElementList(parent)
            when (parent) {
                is IonStruct -> path += Element.Field(value.fieldName)
                is IonSequence -> path += Element.Index(parent.indexOf(value))
                else -> TODO("Unreachable!")
            }
            return path
        }
    }
}
