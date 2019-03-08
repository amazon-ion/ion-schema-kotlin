## Ion Schema Tests

This collection of files represents a test suite for implementations of the [Ion Schema Specification](https://amzn.github.io/ion-schema/docs/spec.html).
This file describes how the tests are defined.

General notes:
* the directory and file structure exists primarily for organizational purposes and is not relevant to successful execution of the tests (with the exception
of tests within the schema directory that rely on schema import functionality).
* when a value of type 'document' needs to be expressed, it is encoded as a string with the `document` annotation.  For example:  `document::"1 2 3"`,
or as a multi-line string:
> ```
> document::'''
>   1
>   2
>   3
> '''
> ```

Tests are defined using one of the following forms:

### Type definition test
To assert expected behavior of one or more constraints, a type definition is provided followed by a list of valid and/or invalid values.
Each value in the `valid` and `invalid` lists is expected to be valid (or invalid) against the preceeding type definition.

For example:
> ```
> type::{
>   type: int,
> }
> valid::[-1, 0, 1]       // all valid ints
> invalid::[a, "b", []]   // none of these are ints
> ```

### Schema definition test
To assert expected behavior of types within a schema, a schema definition is provided followed by a list of valid and/or invalid values
for a specified type from the schema.

For example:
> ```
> schema_header::{}
> type::{
>   name: char,
>   codepoint_length: 1,
> }
> type::{
>   name: char_list,
>   type: list,
>   element: char,
> }
> schema_footer::{}
> valid::{
>   char: [a, b, c],                  // list of valid chars
>   char_list: [[d], [e, f, g]],      // list of valid char_lists
> }
> invalid::{
>   char: [ab, cd, de],               // list of invalid chars
>   char_list: [[ab], [ab, cd, de]],  // list of invalid char_lists
> }
> ```

### Invalid type definition test
To assert that a type definition should be recognized as invalid, the `invalid_type` annotation is used.

For example:
> ```
> invalid_type::{ type: 5 }           // '5' is not a valid type reference
> ```

### Invalid schema definition test
To assert that a schema definition should be recognized as invalid, the `invalid_schema` annotation is used.

For example:
> ```
> invalid_schema::document::'''
>   schema_header::{}
>   type::{ type: unknown_type }      // this type reference is invalid
>   schema_footer::{}
> '''
> ```

### Validation details test
To assert that the Violations object returned by `Type.validate()` includes the expected details, a type or schema definition is provided,
followed by a struct with the `test_validation` annotation.

For example:
> ```
> type::{
>   fields: {
>     age: int,
>   },
>   type: $any,
> }
> test_validation::{
>   value: {age: five},       // the value to test
> 
>   // alternatively, mutliple test values may be specified:
>   // values: [{age: five}, {age: null}],
> 
>   // in the context of a schema, the type to validate against must be specified, e.g.:
>   // type: X,
> 
>   violations: [             // corresponds to a Violations object returned by Type.validate()
>     {
>       constraint: { fields: { age: int } },
>       code: fields_mismatch,
>       children: [
>         {
>           fieldName: "age",
>           value: five,
>           violations: [ { constraint: { type: int }, code: type_mismatch } ],
>         },
>       ],
>     },
>   ],
> }
> ```

