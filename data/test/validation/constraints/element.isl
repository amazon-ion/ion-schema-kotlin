type::{
  element: int,
  type: $any,
}

test_validation::{
  value: hi,
  violations: [
    { constraint: { element: int }, code: invalid_type },
  ],
}

test_validation::{
  values: (null.list null.sexp null.struct),
  violations: [
    { constraint: { element: int }, code: null_value },
  ],
}

test_validation::{
  values: (("a" 5 b) ["a", 5, b] {a: "a", b: 5, c: b}),
  violations: [
    {
      constraint: { element: int },
      code: element_mismatch,
      children: [
        {
          index: 0,
          value: "a",
          violations: [
            { constraint: { type: int }, code: type_mismatch },
          ],
        },
        {
          index: 2,
          value: b,
          violations: [
            { constraint: { type: int }, code: type_mismatch },
          ],
        },
      ],
    },
  ],
}

test_validation::{
  value: (0 5.0),
  violations: [
    {
      constraint: { element: int },
      code: element_mismatch,
      children: [
        {
          index: 1,
          value: 5.0,
          violations: [
            { constraint: { type: int }, code: type_mismatch },
          ],
        },
      ],
    },
  ],
}


schema_header::{}
type::{
  name: CustomInt,
  type: int,
}
type::{
  name: CollectionOfCustomInt,
  element: CustomInt,
}
schema_footer::{}

test_validation::{
  value: (0 1.0),
  type: CollectionOfCustomInt,
  violations: [
    {
      constraint: { element: CustomInt },
      code: element_mismatch,
      children: [
        {
          index: 1,
          value: 1.0,
          violations: [
            {
              constraint: { type: CustomInt },
              code: type_mismatch,
              violations: [
                { constraint: { type: int }, code: type_mismatch },
              ],
            },
          ],
        },
      ],
    },
  ],
}


schema_header::{}
type::{
  name: String5,
  codepoint_length: 5,
  type: string,
}
type::{
  name: SimpleStruct,
  element: {
    fields: {
      a: int,
      b: String5,
    },
  },
}
schema_footer::{}

test_validation::{
  value: ({a: 5, b: "hello"} {a: 1, b: "hi"} {a: true, b: hi}),
  type: SimpleStruct,
  violations: [
    {
      constraint: {
        element: {
          fields: {
            a: int,
            b: String5,
          },
        },
      },
      code: element_mismatch,
      children: [
        {
          index: 1,
          value: { a: 1, b: "hi" },
          violations: [
            {
              constraint: { fields: { a: int, b: String5 } },
              code: fields_mismatch,
              children: [
                {
                  path: "b",
                  value: "hi",
                  violations: [
                    {
                      constraint: { type: String5 },
                      code: type_mismatch,
                      violations: [
                        { constraint: { codepoint_length: 5 }, code: invalid_codepoint_length },
                      ],
                    },
                  ],
                },
              ],
            },
          ],
        },
        {
          index: 2,
          value: { a: true, b: hi },
          violations: [
            {
              constraint: { fields: { a: int, b: String5 } },
              code: fields_mismatch,
              children: [
                {
                  path: "a",
                  value: true,
                  violations: [
                    { constraint: { type: int }, code: type_mismatch },
                  ],
                },
                {
                  path: "b",
                  value: hi,
                  violations: [
                    {
                      constraint: { type: String5 },
                      code: type_mismatch,
                      violations: [
                        { constraint: { codepoint_length: 5 }, code: invalid_codepoint_length },
                        { constraint: { type: string }, code: type_mismatch },
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

