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

