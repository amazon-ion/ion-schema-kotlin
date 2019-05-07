type::{
  ordered_elements: [
    int,
    decimal,
    string,
  ],
}
test_validation::{
  values: [
    [true, 5, hello],
    document::"true 5 hello",
  ],
  violations: [
    {
      constraint: { ordered_elements: [int, decimal, string] },
      code: ordered_elements_mismatch,
    },
  ],
}
test_validation::{
  values: [
    [5, 5.0, "hi", extra_content, more_extra_content],
    document::'''5 5.0 "hi" extra_content more_extra_content''',
  ],
  violations: [
    {
      constraint: { ordered_elements: [ int, decimal, string ] },
      code: ordered_elements_mismatch,
    },
  ],
}

