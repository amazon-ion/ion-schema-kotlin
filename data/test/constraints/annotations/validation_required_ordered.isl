type::{
  annotations: required::ordered::[a, b, c],
}
test_validation::{
  value: c::b::a::5,
  violations: [
    {
      constraint: { annotations: required::ordered::[a, b, c] },
      code: annotations_mismatch,
    },
  ],
}

