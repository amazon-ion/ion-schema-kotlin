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
import software.amazon.ionschema.IonSchemaSystemBuilder;
import software.amazon.ionschema.Schema;
import software.amazon.ionschema.Type;
import software.amazon.ionschema.Violations;

public class IonSchemaGettingStarted {
    private static IonSystem ION = IonSystemBuilder.standard().build();

    public static void main(String[] args) {
        IonSchemaSystem iss = IonSchemaSystemBuilder.standard()
                .withAuthority(new AuthorityFilesystem("<base_path>/data/test"))
                .build();

        Schema schema = iss.loadSchema("/schema/Customer.isl");
        Type type = schema.getType("Customer");

        checkValue(type, "{ firstName: \"Susie\", lastName: \"Smith\" }");
        checkValue(type, "{ firstName: \"Susie\", middleName: \"B\", lastName: \"Smith\" }");
        checkValue(type, "{ middleName: B, lastName: Washington }");
    }

    private static void checkValue(Type type, String str) {
        IonValue value = ION.singleValue(str);
        Violations violations = type.validate(value);
        if (!violations.isValid()) {
            System.out.println(str);
            System.out.println(violations);
        }
    }
}
```

When run, the code above produces the following output:
```
{ middleName: B, lastName: Washington }
Validation failed:
- one or more fields don't match expectations
  - firstName
    - expected range::[1,1] occurrences, found 0
  - middleName: B
    - expected type string, found symbol
  - lastName: Washington
    - expected type string, found symbol
    - invalid codepoint length 10, expected range::[min,7]
```

## Roadmap

The roadmap is organized as a series of [milestones](https://github.com/amzn/ion-schema-kotlin/milestones?direction=asc&sort=due_date&state=open).

## License

This library is licensed under the Apache 2.0 License. 
