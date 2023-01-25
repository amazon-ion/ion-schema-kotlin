## Ion Schema Kotlin

A reference implementation of the [Ion Schema Specification](https://amazon-ion.github.io/ion-schema/docs/spec.html),
written in Kotlin.

[![Build Status](https://travis-ci.org/amazon-ion/ion-schema-kotlin.svg)](https://travis-ci.org/amazon-ion/ion-schema-kotlin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.amazon.ion/ion-schema-kotlin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.amazon.ion/ion-schema-kotlin)
[![Javadoc](https://javadoc-badge.appspot.com/com.amazon.ion/ion-schema-kotlin.svg?label=javadoc)](http://www.javadoc.io/doc/com.amazon.ion/ion-schema-kotlin)
[![codecov](https://codecov.io/gh/amazon-ion/ion-schema-kotlin/branch/master/graph/badge.svg)](https://codecov.io/gh/amazon-ion/ion-schema-kotlin)

## Getting Started

The following kotlin and java code samples are a simple example of how to use this
API. The Customer type is a struct that requires `firstName` and `lastName` fields as strings, while the
`middleName` field is optional ( *To keep things simple, the schema is loaded
inline* ).

#### Kotlin
```kotlin
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.IonSchemaSystemBuilder.Companion.standard
import com.amazon.ionschema.Type


object IonSchemaGettingStarted {
    private val ION = IonSystemBuilder.standard().build()
    @JvmStatic
    fun main(args: Array<String>) {
        val iss = standard()
                .build()

        val schema = iss.newSchema(
                """
            type::{
              name: Customer,
              type: struct,
              fields: {
                firstName: { type: string, occurs: required },
                middleName: string,
                lastName: { type: string, occurs: required },
              },
            }
        """
        )
        val type = schema.getType("Customer")
        checkValue(type, """ { firstName: "Susie", lastName: "Smith" } """)
        checkValue(type, """ { firstName: "Susie", middleName: "B", lastName: "Smith" } """)
        checkValue(type, """ { middleName: B, lastName: Washington } """)
    }

    private fun checkValue(type: Type?, str: String) {
        val value = ION.singleValue(str)
        val violations = type!!.validate(value)
        if (!violations.isValid()) {
            println(str)
            println(violations)
        }
    }
}
```

#### Java
```java
import com.amazon.ion.IonSystem;
import com.amazon.ion.IonValue;
import com.amazon.ion.system.IonSystemBuilder;
import com.amazon.ionschema.AuthorityFilesystem;
import com.amazon.ionschema.IonSchemaSystem;
import com.amazon.ionschema.IonSchemaSystemBuilder;
import com.amazon.ionschema.Schema;
import com.amazon.ionschema.Type;
import com.amazon.ionschema.Violations;

public class IonSchemaGettingStarted {
    private static IonSystem ION = IonSystemBuilder.standard().build();

    public static void main(String[] args) {
        IonSchemaSystem iss = IonSchemaSystemBuilder.standard()
                .build();

        Schema schema = iss.newSchema(" type::{\n" +
                        "              name: Customer,\n" +
                        "              type: struct,\n" +
                        "              fields: {\n" +
                        "                firstName: { type: string, occurs: required },\n" +
                        "                middleName: string,\n" +
                        "                lastName: { type: string, occurs: required },\n" +
                        "              },\n" +
                        "            }\n")
                ;
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
```

## Development
This repository contains two [git submodules](https://git-scm.com/docs/git-submodule):
[ion-schema-tests](https://github.com/amazon-ion/ion-schema-tests)
and [ion-schema-schemas](https://github.com/amazon-ion/ion-schema-schemas).
Both are used by this library's unit tests.

The easiest way to clone the `ion-schema-kotlin` repository and initialize its submodules
is to run the following command:

```
$ git clone --recursive https://github.com/amazon-ion/ion-schema-kotlin.git ion-schema-kotlin
```

Alternatively, the submodule may be initialized independently from the clone
by running the following commands:

```
$ git submodule init
$ git submodule update
```

`ion-schema-kotlin` may now be built with the following command:

```
$ gradle build
```

### Pulling in Upstream Changes
To pull upstream changes into `ion-schema-kotlin`, start with a simple `git pull`.
This will pull in any changes to `ion-schema-kotlin` itself (including any changes
to its `.gitmodules` file), but not any changes to the submodules.
To make sure the submodules are up-to-date, use the following
command:

```
$ git submodule update --remote
```

For detailed walkthroughs of git submodule usage, see the
[Git Tools documentation](https://git-scm.com/book/en/v2/Git-Tools-Submodules).


## Roadmap

The roadmap is organized as a series of [milestones](https://github.com/amazon-ion/ion-schema-kotlin/milestones?direction=asc&sort=due_date&state=open).

## License

This library is licensed under the Apache 2.0 License. 

