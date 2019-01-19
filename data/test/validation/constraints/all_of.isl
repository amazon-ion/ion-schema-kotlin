type::{
  all_of: [
    { type: symbol },
    { codepoint_length: 5 },
  ],
}

test_validation::{
  value: "abcde",
  violations: [
    {
      constraint: {
        all_of: [
          { type: symbol },
          { codepoint_length: 5 },
        ],
      },
      code: all_types_not_matched,
      violations: [
        {
          constraint: { type: symbol },
          code: type_mismatch,
        },
      ],
    },
  ],
}

