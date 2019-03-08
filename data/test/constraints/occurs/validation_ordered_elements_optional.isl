type::{
  ordered_elements: [
    { type: int, occurs: 2 },
    { type: symbol, occurs: range::[0, 1] },
    { type: string, occurs: range::[1, 3] },
  ],
}

test_validation::{
  values: ([ 1, hello ] document::"1 hello"),
  violations: [
    {
      constraint: {
        ordered_elements: [
          { type: int, occurs: 2 },
          { type: symbol, occurs: range::[0, 1] },
          { type: string, occurs: range::[1, 3] },
        ],
      },
      code: ordered_elements_mismatch,
      children: [
        {
          index: 1,
          value: hello,
          violations: [
            {
              constraint: { type: int },
              code: type_mismatch,
            },
          ],
        },
        {
          index: 2,
          violations: [
            {
              constraint: { occurs: range::[1, 3] },
              code: occurs_mismatch,
            },
          ],
        },
      ],
    },
  ],
}

test_validation::{
  values: ([ 1, 2, hello, "1", "2", "3", "4", "5" ] document::'''1 2 hello "1" "2" "3" "4" "5"'''),
  violations: [
    {
      constraint: {
        ordered_elements: [
          { type: int, occurs: 2 },
          { type: symbol, occurs: range::[0, 1] },
          { type: string, occurs: range::[1, 3] },
        ],
      },
      code: unexpected_content,
      children: [
        { index: 6, value: "4" },
        { index: 7, value: "5" },
      ],
    },
  ],
}

