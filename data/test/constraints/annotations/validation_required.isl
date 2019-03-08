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

