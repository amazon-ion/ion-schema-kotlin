type::{
  codepoint_length: 5,
  type: $any,
}

test_validation::{
  value: null,
  violations: [
    { constraint: { codepoint_length: 5 }, code: invalid_type },
  ],
}

test_validation::{
  values: [null.string, null.symbol],
  violations: [
    { constraint: { codepoint_length: 5 }, code: null_value },
  ],
}

test_validation::{
  values: [abcd, abcdef],
  violations: [
    { constraint: { codepoint_length: 5 }, code: invalid_codepoint_length },
  ],
}

