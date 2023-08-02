package com.amazon.ionschema.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class SchemaSymbolsUtilJavaApiTest {

    @Test
    public void testApi() {
        // Test just to make sure that these functions are nicely usable from Java
        Set<String> symbolTexts = SchemaSymbolsUtil.getSymbolsTextForPath(
                "./src/main/resources/ion-schema-schemas/",
                // This lambda could be omitted, except that the ion-schema-schemas directory contains ISL 1.0, and
                // the SchemaSymbolsUtil doesn't support that yet.
                f -> f.getPath().contains("isl/ion_schema_2_0")
        );
        Assertions.assertEquals(SchemaSymbolsUtilTest.ION_SCHEMA_2_0_SYMBOLS, symbolTexts);
    }
}
