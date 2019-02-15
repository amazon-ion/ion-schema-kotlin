type::{
  valid_values: [ null, null.string, 5, hello ],
  type: $any,
}
test_validation::{
  values: [ null.symbol, 6, hi, "hello" ],
  violations: [
    {
      constraint: { valid_values: [ null, null.string, 5, hello ] },
      code: invalid_value,
    },
  ],
}

type::{
  valid_values: range::[1, 9],
}
test_validation::{
  values: [0, 10],
  violations: [
    {
      constraint: { valid_values: range::[1, 9] },
      code: invalid_value,
    }
  ],
}

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

