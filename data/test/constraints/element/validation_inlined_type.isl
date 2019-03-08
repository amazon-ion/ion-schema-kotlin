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
                  fieldName: "b",
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
                  fieldName: "a",
                  value: true,
                  violations: [
                    { constraint: { type: int }, code: type_mismatch },
                  ],
                },
                {
                  fieldName: "b",
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

