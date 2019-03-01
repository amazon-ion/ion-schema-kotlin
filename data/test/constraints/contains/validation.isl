type::{
  contains: [a, b, c],
  type: $any,
}

test_validation::{
  value: 5,
  violations: [
    { constraint: { contains: [a, b, c] }, code: invalid_type },
  ],
}

test_validation::{
  values: (null.list null.sexp null.struct),
  violations: [
    { constraint: { contains: [a, b, c] }, code: null_value },
  ],
}

test_validation::{
  values: ([] () {} document::""),
  violations: [
    { constraint: { contains: [a, b, c] }, code: missing_values },
  ],
}

