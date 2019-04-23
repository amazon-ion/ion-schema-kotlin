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
    },
  ],
}

