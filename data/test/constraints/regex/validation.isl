type::{
  regex: i::"susie",
  type: $any,
}

test_validation::{
  value: 5,
  violations: [
    { constraint: { regex: i::"susie" }, code: invalid_type },
  ],
}

test_validation::{
  values: [null.string, null.symbol],
  violations: [
    { constraint: { regex: i::"susie" }, code: null_value },
  ],
}

test_validation::{
  values: ["susan", susan],
  violations: [
    { constraint: { regex: i::"susie" }, code: regex_mismatch },
  ],
}

