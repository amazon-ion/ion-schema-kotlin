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

