type::{
  precision: 2,
  type: $any,
}

test_validation::{
  value: null.decimal,
  violations: [
    { constraint: { precision: 2 }, code: null_value },
  ],
}

test_validation::{
  value: 5,
  violations: [
    { constraint: { precision: 2 }, code: invalid_type },
  ],
}

test_validation::{
  values: [ 5., 1.23 ],
  violations: [
    { constraint: { precision: 2 }, code: invalid_precision },
  ],
}

