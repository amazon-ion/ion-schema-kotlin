type::{
  timestamp_precision: range::[second, millisecond],
  type: $any,
}

test_validation::{
  value: null.decimal,
  violations: [
    { constraint: { timestamp_precision: range::[second, millisecond] }, code: invalid_type },
  ],
}

test_validation::{
  value: null.timestamp,
  violations: [
    { constraint: { timestamp_precision: range::[second, millisecond] }, code: null_value },
  ],
}

test_validation::{
  value: 2000-01-01T,
  violations: [
    { constraint: { timestamp_precision: range::[second, millisecond] }, code: invalid_timestamp_precision },
  ],
}

