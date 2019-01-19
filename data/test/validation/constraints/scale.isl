type::{
  scale: 2,
  type: $any,
}

test_validation::{
  value: null.decimal,
  violations: [
    { constraint: { scale: 2 }, code: null_value },
  ],
}

test_validation::{
  values: [ 5, 5e0 ],
  violations: [
    { constraint: { scale: 2 }, code: invalid_type },
  ],
}

test_validation::{
  values: [ 1.2, 1.234 ],
  violations: [
    { constraint: { scale: 2 }, code: invalid_scale },
  ],
}

