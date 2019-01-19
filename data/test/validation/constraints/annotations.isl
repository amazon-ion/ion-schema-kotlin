type::{
  annotations: required::[a, b, c],
}
test_validation::{
  value: 5,
  violations: [
    {
      constraint: { annotations: required::[a, b, c] },
      code: missing_annotation,
    },
  ],
}

/*
TBD
type::{
  annotations: ordered::[a, b, c],
}
test_validation::{
  value: c::b::a::5,
  violations: [
    {
      constraint: { annotations: ordered::[a, b, c] },
      code: invalid_annotation_order,
    },
  ],
}
*/
