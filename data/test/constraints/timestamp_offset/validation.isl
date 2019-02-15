type::{
  timestamp_offset: ["-08:00"],
  type: $any,
}

test_validation::{
  value: null.decimal,
  violations: [
    { constraint: { timestamp_offset: ["-08:00"] }, code: invalid_type },
  ],
}

test_validation::{
  value: null.timestamp,
  violations: [
    { constraint: { timestamp_offset: ["-08:00"] }, code: null_value },
  ],
}

test_validation::{
  value: 2000-01-01T,
  violations: [
    { constraint: { timestamp_offset: ["-08:00"] }, code: invalid_timestamp_offset },
  ],
}

