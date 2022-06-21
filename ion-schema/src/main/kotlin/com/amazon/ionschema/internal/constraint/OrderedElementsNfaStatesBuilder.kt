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
 * Builds states and edges for a [NFA] for the `ordered_elements` constraint.
 *
 * In this case, the `NFA`'s `Event` type parameter is an [IonValue] from the input that is being validated by the
 * Ion Schema System, and [NFA.State] is used to model an entry in the `ordered_elements` list.
 *
 * ### Example
 * Given this ISL fragment:
 * ```ion
 * ordered_elements: [
 *   symbol,
 *   { type: number, occurs: optional },
 *   { type: int, occurs: range::[2, 4] },
 * ]
 * ```
 * The [OrderedElementsNfaStatesBuilder] would create the following states:
 * ```markdown
 * | ID |     Entry Condition     | Re-entry Condition | Exit Condition |
 * |----|:-----------------------:|:------------------:|:--------------:|
 * | 1  | ionValue.type == symbol |     visits <= 1    |   visits >= 1  |
 * | 2  | ionValue.type == number |     visits <= 1    |   visits >= 0  |
 * | 3  | ionValue.type == int    |     visits <= 4    |   visits >= 2  |
 * ```
 * And then combine them to form this graph:
 * ```
 *                               +--+
 *                               |  |
 *                               |  v
 * +------+      +------+      +------+     +-------+
 * | INIT |----> |  S1  |----> |  S3  |---->| FINAL |
 * +------+      +------+      +------+     +-------+
 *                  |              ^
 *                  v              |
 *               +------+          |
 *               |  S2  |----------+
 *               +------+
 * ```
 * Once these have been used to construct the [NFA] instance, the [OrderedElements] constraint implementation can call
 * [NFA.matches] to determine if a sequence of Ion values is accepted by the NFA.
 *
 * When there are no ambiguous choices, it is simple.
 * Input: `(foo 1.0 2 3)`
 * ```
 *      : [I:1]
 *    0 : [S1:1]
 *    1 : [S2:1]
 *    2 : [S3:1]
 *    3 : [S3:2]
 *  END : [F:1]
 * ```
 *
 * In this case we have an ambiguous choice—the `1` could be the optional `number` or it could be one of the 2 or more `int`s.
 * Input: `(foo 1 2 3)`
 * ```
 *      : [I:1]
 *    0 : [S1:1]
 *    1 : [S2:1, S3:1]  <-- Ambiguous choice, so track both possible paths
 *    2 : [S3:1, S3:2]
 *    3 : [S3:2, S3:3]  <-- Note that the paths are at the same state, but
 *  END : [F:1]             each has a different number of visits.
 * ```
 * When there are no valid transitions, it  transitions to "nowhere" (ie. the path through the graph "dies off").
 * Input: `(foo 1 "Hi!")`
 * ```
 *      : [I:1]
 *    0 : [S1:1]
 *    1 : [S2:1, S3:1]  <-- Neither one of these paths have a valid transition for `"Hi!"`,
 *    2 : []                so they both die off. There are 0 possible paths remaining.
 *  END : []
 * ```
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
