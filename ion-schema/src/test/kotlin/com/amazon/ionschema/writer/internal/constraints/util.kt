// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ion.system.IonSystemBuilder

private val ION = IonSystemBuilder.standard().build()

/** Helper fun for creating IonValue instances */
fun ion(text: String) = ION.singleValue(text)
