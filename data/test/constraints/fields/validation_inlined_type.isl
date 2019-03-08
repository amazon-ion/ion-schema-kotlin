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

