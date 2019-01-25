type::{
  fields: {
    one: int,
  },
  type: $any,
}

test_validation::{
  value: null.struct,
  violations: [
    { constraint: { fields: { one: int } }, code: null_value },
  ],
}

test_validation::{
  value: 5,
  violations: [
    { constraint: { fields: { one: int } }, code: invalid_type },
  ],
}

test_validation::{
  value: { one: 1.0 },
  violations: [
    {
      constraint: { fields: { one: int } },
      code: fields_mismatch,
      children: [
        {
          fieldName: "one",
          value: 1.0,
          violations: [ { constraint: { type: int }, code: type_mismatch } ],
        },
      ],
    },
  ],
}

type::{
  type: struct,
  fields: {
    one: {
      type: struct,
      fields: {
        two: int,
      },
    },
  },
}
test_validation::{
  value: { one: { two: 2.0 } },
  violations: [
    {
      constraint: { fields: { one: { type: struct, fields: { two: int } } } },
      code: fields_mismatch,
      children: [
        {
          fieldName: "one",
          value: { two: 2.0 },
          violations: [
            {
              constraint: { fields: { two: int } },
              code: fields_mismatch,
              children: [
                {
                  fieldName: "two",
                  value: 2.0,
                  violations: [
                    { constraint: { type: int }, code: type_mismatch },
                  ],
                },
              ],
            },
          ],
        },
      ],
    },
  ],
}

type::{
  type: struct,
  fields: {
    one: int,
    two: symbol,
    three: { type: symbol, codepoint_length: range::[4, 6] },
    four: { type: decimal, occurs: required },
  },
}
test_validation::{
  value: { one: 1.0, two: "hi", three: "hello world" },
  violations: [
    {
      constraint: {
        fields: {
          one: int,
          two: symbol,
          three: { type: symbol, codepoint_length: range::[4, 6] },
          four: { type: decimal, occurs: required },
        },
      },
      code: fields_mismatch,
      children: [
        {
          fieldName: "one",
          value: 1.0,
          violations: [
            { constraint: { type: int }, code: type_mismatch },
          ],
        },
        {
          fieldName: "two",
          value: "hi",
          violations: [
            { constraint: { type: symbol }, code: type_mismatch },
          ],
        },
        {
          fieldName: "three",
          value: "hello world",
          violations: [
            { constraint: { type: symbol }, code: type_mismatch },
            { constraint: { codepoint_length: range::[4, 6] }, code: invalid_codepoint_length },
          ],
        },
        {
          fieldName: "four",
          violations: [
            { constraint: { occurs: required }, code: occurs_mismatch },
          ],
        },
      ],
    },
  ],
}


schema_header::{}
type::{
  name: ShortString,
  codepoint_length: range::[0, 10],
  type: string,
}
type::{
  name: Book,
  type: struct,
  fields: {
    pretitle: string,
    title: ShortString,
    subtitle: { type: string, codepoint_length: 5 },
    subsubtitle: type::{ type: string, codepoint_length: 5 },
  }
}
type::{
  name: Person,
  type: struct,
  fields: {
    favoriteBook: Book,
  }
}
schema_footer::{}

test_validation::{
  type: Person,
  value: {
    favoriteBook: {
      pretitle: blah,
      title: "The Title of this Book is Too Long",
      subtitle: "abc",
      subsubtitle: "xyz",
    },
  },
  violations: [
    {
      constraint: { fields: { favoriteBook: Book } },
      code: fields_mismatch,
      children: [
        {
          fieldName: "favoriteBook",
          value: {
            pretitle: blah,
            title: "The Title of this Book is Too Long",
            subtitle: "abc",
            subsubtitle: "xyz",
          },
          violations: [
            {
              constraint: { type: Book },
              code: type_mismatch,
              violations: [
                {
                  constraint: {
                    fields: {
                      pretitle: string,
                      title: ShortString,
                      subtitle: { type: string, codepoint_length: 5 },
                      subsubtitle: type::{ type: string, codepoint_length: 5 },
                    },
                  },
                  code: fields_mismatch,
                  children: [
                    {
                      fieldName: "pretitle",
                      value: blah,
                      violations: [
                        {
                          constraint: { type: string },
                          code: type_mismatch,
                        },
                      ],
                    },
                    {
                      fieldName: "title",
                      value: "The Title of this Book is Too Long",
                      violations: [
                        {
                          constraint: { type: ShortString },
                          code: type_mismatch,
                          violations:[
                            {
                              constraint: { codepoint_length: range::[0, 10] },
                              code: invalid_codepoint_length,
                            },
                          ],
                        },
                      ],
                    },
                    {
                      fieldName: "subtitle",
                      value: "abc",
                      violations: [
                        {
                          constraint: { codepoint_length: 5 },
                          code: invalid_codepoint_length,
                        },
                      ],
                    },
                    {
                      fieldName: "subsubtitle",
                      value: "xyz",
                      violations: [
                        {
                          constraint: { codepoint_length: 5 },
                          code: invalid_codepoint_length,
                        },
                      ],
                    },
                  ],
                },
              ],
            },
          ],
        },
      ],
    },
  ],
}

