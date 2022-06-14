/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazon.ionschema.internal.constraint

import com.amazon.ion.IonValue

/**
 * Builds a NFA for the `ordered_elements` constraint
 */
internal class OrderedElementsNfaStatesBuilder {

    private val stateInputs = mutableListOf<NFA.State<IonValue>>(NFA.State.Initial)

    fun addState(min: Comparable<Int>, max: Comparable<Int>, matches: (IonValue) -> Boolean): OrderedElementsNfaStatesBuilder {
        val nfaState = NFA.State.Intermediate(
            id = stateInputs.size,
            entryCondition = { event: IonValue -> matches(event) },
            reentryCondition = { visits -> max >= visits },
            exitCondition = { visits -> min <= visits }
        )
        stateInputs.add(nfaState)
        return this
    }

    fun build(): Map<NFA.State<IonValue>, Set<NFA.State<IonValue>>> {
        val states = stateInputs + NFA.State.Final

        val transitions = states.mapIndexed { i, stateI ->
            val transitionsForStateI = mutableSetOf<NFA.State<IonValue>>()

            // Loop back to self, if max is >= 2
            if (stateI.canReenter(2)) transitionsForStateI += stateI

            // Add transitions forward up to (including) the first type with a min occurs that is greater than 0
            var j = i + 1
            while (j < states.size) {
                val stateJ = states[j]
                transitionsForStateI += stateJ
                // Hack to check if the state is optional. It's safe to do this because the construction of the canExit
                // condition is also controlled by this class.
                if (!stateJ.canExit(0)) break
                j++
            }
            stateI to transitionsForStateI.toSet()
        }

        return transitions.toMap()
    }
}
