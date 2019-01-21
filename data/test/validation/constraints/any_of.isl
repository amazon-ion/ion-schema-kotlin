type::{
  any_of: [symbol, string],
  type: $any,
}

test_validation::{
  value: null.string,
  violations: [
    {
      constraint: { any_of: [symbol, string] },
      code: no_types_matched,
      violations: [
        { constraint: { type: symbol }, code: type_mismatch },
        { constraint: { type: string }, code: null_value },
      ],
    },
  ],
}

