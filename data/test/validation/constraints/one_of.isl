type::{
  one_of: [
    symbol,
    { codepoint_length: 5 },
    { regex: "12345" },
  ],
}

test_validation::{
  value: 1,
  violations: [
    {
      constraint: {
        one_of: [
          symbol,
          { codepoint_length: 5 },
          { regex: "12345" },
        ],
      },
      code: no_types_matched,
      violations: [
        { constraint: { type: symbol }, code: type_mismatch },
        {
          constraint: { codepoint_length: 5 },
          code: type_mismatch,
          violations: [
            { constraint: { codepoint_length: 5 }, code: invalid_type },
          ],
        },
        {
          constraint: { regex: "12345" },
          code: type_mismatch,
          violations: [
            { constraint: { regex: "12345" }, code: invalid_type },
          ],
        },
      ],
    },
  ],
}

test_validation::{
  value: '12345',
  violations: [
    {
      constraint: {
        one_of: [
          symbol,
          { codepoint_length: 5 },
          { regex: "12345" },
        ],
      },
      code: more_than_one_type_matched,
      violations: [
        { constraint: { type: symbol }, code: type_matched },
        { constraint: { codepoint_length: 5 }, code: type_matched },
        { constraint: { regex: "12345" }, code: type_matched },
      ],
    },
  ],
}

