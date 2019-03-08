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
  values: [
    (true {}),
    document::"true {}",
  ],
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

