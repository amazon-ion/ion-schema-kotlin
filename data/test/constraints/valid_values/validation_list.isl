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

