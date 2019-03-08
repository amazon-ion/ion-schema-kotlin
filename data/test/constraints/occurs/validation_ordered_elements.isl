type::{
  ordered_elements: [
    { type: int, occurs: range::[2, 4] },
  ],
}

test_validation::{
  values: ([ 1 ] document::"1"),
  violations: [
    {
      constraint: {
        ordered_elements: [
          { type: int, occurs: range::[2, 4] },
        ],
      },
      code: ordered_elements_mismatch,
      children: [
        {
          index: 1,
          violations: [
            {
              constraint: { occurs: range::[2, 4] },
              code: occurs_mismatch,
            },
          ],
        },
      ],
    },
  ],
}

