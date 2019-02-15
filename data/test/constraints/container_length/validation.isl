type::{
  container_length: range::[1, 3],
  type: $any,
}

test_validation::{
  value: 5,
  violations: [
    { constraint: { container_length: range::[1, 3] }, code: invalid_type },
  ],
}

test_validation::{
  values: (null.list null.sexp null.struct),
  violations: [
    { constraint: { container_length: range::[1, 3] }, code: null_value },
  ],
}

test_validation::{
  values: ([] () {} [1,2,3,4] (1 2 3 4) {a:1, b:2, c:3, d:4}),
  violations: [
    { constraint: { container_length: range::[1, 3] }, code: invalid_container_length },
  ],
}

