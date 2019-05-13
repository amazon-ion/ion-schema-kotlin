/*
 * Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonContainer
import software.amazon.ion.IonValue

/**
 * Implements the container_length constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#container_length
 */
internal class ContainerLength(
        ion: IonValue
) : ConstraintBaseIntRange<IonContainer>(IonContainer::class.java, ion) {

    override val violationCode = "invalid_container_length"
    override val violationMessage = "invalid container length %s, expected %s"

    override fun getIntValue(value: IonContainer) = value.size()
}

