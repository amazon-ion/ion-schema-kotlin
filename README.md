## Ion Schema Kotlin

A reference implementation of the [Ion Schema Specification](https://amzn.github.io/ion-schema/docs/spec.html),
written in Kotlin.

[![Build Status](https://travis-ci.org/amzn/ion-schema-kotlin.svg)](https://travis-ci.org/amzn/ion-schema-kotlin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/software.amazon.ion/ion-schema-kotlin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/software.amazon.ion/ion-schema-kotlin)
[![Javadoc](https://javadoc-badge.appspot.com/software.amazon.ion/ion-schema-kotlin.svg?label=javadoc)](http://www.javadoc.io/doc/software.amazon.ion/ion-schema-kotlin)

**This is currently alpha software and all aspects of it are subject
to change.**

## Getting Started

The following code provides a simple example of how to use this
API from Java. The Customer type is a struct that requires
`firstName` and `lastName` fields as strings, while the
`middleName` field is optional.

Before running, replace `<base_path>` with the path containing
"/data/test".

```java
import software.amazon.ion.IonSystem;
import software.amazon.ion.IonValue;
import software.amazon.ion.system.IonSystemBuilder;
import software.amazon.ionschema.AuthorityFilesystem;
import software.amazon.ionschema.IonSchemaSystem;
import software.amazon.ionschema.Schema;
import software.amazon.ionschema.Type;

public class IonSchemaGettingStarted {
    private static IonSystem ION = IonSystemBuilder.standard().build();

    public static void main(String[] args) {
        IonSchemaSystem iss = IonSchemaSystem.Builder.standard()
                .withAuthority(new AuthorityFilesystem("<base_path>/data/test"))
                .build();

        Schema schema = iss.loadSchema("/schema/Customer.isl");
        Type type = schema.getType("Customer");

        checkValue(type, "{ firstName: \"Susie\", }");
        checkValue(type, "{ firstName: \"Susie\", lastName: \"Smith\" }");
        checkValue(type, "{ firstName: \"Susie\", middleName: \"B\", lastName: \"Smith\" }");
    }

    private static void checkValue(Type type, String str) {
        IonValue value = ION.singleValue(str);
        System.out.println(str + ": " + type.isValid(value));
    }
}
```

## Helping out

The issues identified below represent the next steps for this project.
Most are relatively small and self-contained;  larger efforts are
shown below in **bold**.

- [ ] [#1](https://github.com/amzn/ion-schema-kotlin/issues/1) add/verify support for schema import
- [ ] [#2](https://github.com/amzn/ion-schema-kotlin/issues/2) **provide validation details when a value is invalid**
- [ ] [#3](https://github.com/amzn/ion-schema-kotlin/issues/3) add support for open/closed content
- [ ] [#4](https://github.com/amzn/ion-schema-kotlin/issues/4) implement the annotations constraint
- [ ] [#5](https://github.com/amzn/ion-schema-kotlin/issues/5) implement the timestamp_offset constraint
- [ ] [#6](https://github.com/amzn/ion-schema-kotlin/issues/6) implement the timestamp_precision constraint
- [ ] [#7](https://github.com/amzn/ion-schema-kotlin/issues/7) verify the element constraint works for structs
- [ ] [#8](https://github.com/amzn/ion-schema-kotlin/issues/8) update valid_values constraint to support timestamp ranges
- [ ] [#9](https://github.com/amzn/ion-schema-kotlin/issues/9) **assert that constraints are compatible with the type on which they are specified**
- [ ] [#10](https://github.com/amzn/ion-schema-kotlin/issues/10) define a schema for ISL
- [ ] [#11](https://github.com/amzn/ion-schema-kotlin/issues/11) **add support for the document type**

## License

This library is licensed under the Apache 2.0 License. 
