package com.amazon.ionschema

import com.amazon.ion.IonValue
import com.amazon.ion.system.IonSystemBuilder
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FailFastOnInvalidAnnotationsTest {

    companion object {
        const val FOO_BAR_BAZ_SCHEMA_ISL_1 = """
            type::{
              name: symbol_or_int_list,
              one_of:[
                {
                  annotations: closed::required::[symbols],
                  element: symbol,
                },
                {
                  annotations: closed::required::[ints],
                  element: int,
                },
                {
                  annotations: closed::required::[],
                  valid_values: [null],
                }
              ]
            }
        """
        const val FOO_BAR_BAZ_SCHEMA_ISL_2 = "\$ion_schema_2_0 \n $FOO_BAR_BAZ_SCHEMA_ISL_1"
    }
    val ion = IonSystemBuilder.standard().build()

    val values = ion.loader.load(
        """
        symbols::[a, b, c, d, e]
        ints::[1, 2, 3, 4, 5]
        ints::[a, b, c, d, e] // invalid
        null
        """.trimIndent()
    )

    val failFastSystem = IonSchemaSystemBuilder.standard().failFastOnInvalidAnnotations(true).build()
    val failSlowSystem = IonSchemaSystemBuilder.standard().failFastOnInvalidAnnotations(false).build()

    @Test
    fun testAnnotationFastFailImprovesIonSchema1() {
        val typeName = "symbol_or_int_list"
        benchmark("Warmup 1", failSlowSystem, FOO_BAR_BAZ_SCHEMA_ISL_1, typeName, values)
        benchmark("Warmup 2", failFastSystem, FOO_BAR_BAZ_SCHEMA_ISL_1, typeName, values)
        val isl1Baseline = benchmark("ISL 1.0 Baseline", failSlowSystem, FOO_BAR_BAZ_SCHEMA_ISL_1, typeName, values)
        val isl1FastFail = benchmark("ISL 1.0 Fast Fail", failFastSystem, FOO_BAR_BAZ_SCHEMA_ISL_1, typeName, values)
        println("Change: ${(isl1FastFail - isl1Baseline) / isl1Baseline}")

        assertTrue(isl1FastFail > isl1Baseline)
    }

    @Test
    fun testAnnotationFastFailImprovesIonSchema2() {
        val typeName = "symbol_or_int_list"
        benchmark("Warmup 1", failSlowSystem, FOO_BAR_BAZ_SCHEMA_ISL_2, typeName, values)
        benchmark("Warmup 2", failFastSystem, FOO_BAR_BAZ_SCHEMA_ISL_2, typeName, values)
        val isl2Baseline = benchmark("ISL 2.0 Baseline", failSlowSystem, FOO_BAR_BAZ_SCHEMA_ISL_2, typeName, values)
        val isl2FastFail = benchmark("ISL 2.0 Fast Fail", failFastSystem, FOO_BAR_BAZ_SCHEMA_ISL_2, typeName, values)
        println("Change: ${(isl2FastFail - isl2Baseline) / isl2Baseline}")

        assertTrue(isl2FastFail > isl2Baseline)
    }

    private fun benchmark(name: String, iss: IonSchemaSystem, schemaString: String, typeName: String, values: List<IonValue>): Double {
        val schema = iss.newSchema(schemaString)

        val fooType = schema.getType(typeName)!!
        val totalOps = 10000
        val times = (1..30).map {
            val start = System.nanoTime()
            for (i in 1..totalOps) {
                fooType.validate(values[i % values.size])
            }
            val end = System.nanoTime()
            end - start
        }
        // ops/s = ops/t_avg * ns/s
        val opsPerSecond = totalOps / times.average() * 1000000000
        println("$name ops/s = $opsPerSecond")
        return opsPerSecond
    }
}
