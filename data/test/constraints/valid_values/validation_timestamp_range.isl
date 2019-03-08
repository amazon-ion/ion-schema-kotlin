type::{
  valid_values: range::[2000-01-01T00:00Z, 2001-01-01T00:00Z],
}
test_validation::{
  value: 2000T,
  violations: [
    {
      constraint: { valid_values: range::[2000-01-01T00:00Z, 2001-01-01T00:00Z] },
      code: unknown_local_offset,
    }
  ],
}

