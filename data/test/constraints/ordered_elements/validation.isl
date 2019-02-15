type::{
  type: list,
  ordered_elements: [
    int,
    decimal,
    string,
  ],
}
test_validation::{
  value: [true, 5, hello],
  violations: [
    {
      constraint: { ordered_elements: [int, decimal, string] },
      code: ordered_elements_mismatch,
      children: [
        {
          index: 0,
          value: true,
          violations: [
            {
              constraint: { type: int },
              code: type_mismatch,
            },
          ],
        },
        {
          index: 1,
          value: 5,
          violations: [
            {
              constraint: { type: decimal },
              code: type_mismatch,
            },
          ],
        },
        {
          index: 2,
          value: hello,
          violations: [
            {
              constraint: { type: string },
              code: type_mismatch,
            },
          ],
        },
      ],
    },
  ],
}
test_validation::{
  value: [5, 5.0, "hi", extra_content, more_extra_content],
  violations: [
    {
      constraint: { ordered_elements: [ int, decimal, string ] },
      code: unexpected_content,
      children: [
        { index: 3, value: extra_content },
        { index: 4, value: more_extra_content },
      ],
    },
  ],
}

type::{
  ordered_elements: [
    { type: bool,    occurs: required },
    { type: int,     occurs: optional },
    { type: decimal, occurs: optional },
    { type: float,   occurs: optional },
    { type: symbol,  occurs: required },
    { type: bool,    occurs: required },
    { type: string,  occurs: required },
  ],
}
test_validation::{
  value: (true {}),
  violations: [
    {
      constraint: {
        ordered_elements: [
          { type: bool,    occurs: required },
          { type: int,     occurs: optional },
          { type: decimal, occurs: optional },
          { type: float,   occurs: optional },
          { type: symbol,  occurs: required },
          { type: bool,    occurs: required },
          { type: string,  occurs: required },
        ],
      },
      code: ordered_elements_mismatch,
      children: [
        {
          index: 1,
          value: {},
          violations: [
            { constraint: { type: int }, code: type_mismatch },
            { constraint: { type: decimal }, code: type_mismatch },
            { constraint: { type: float }, code: type_mismatch },
            { constraint: { type: symbol }, code: type_mismatch },
          ],
        },
        {
          index: 2,
          violations: [
            { constraint: { occurs: required }, code: occurs_mismatch },
          ],
        },
        {
          index: 2,
          violations: [
            { constraint: { occurs: required }, code: occurs_mismatch },
          ],
        },
      ],
    },
  ],
}

