type::{
  fields: {
    a: { occurs: range::[0, 1] },
    b: { occurs: range::[1, 1] },
    c: { occurs: range::[1, 3] },
  }
}

test_validation::{
  value: {
    a: 1, a: 2,
    c: 1, c: 2, c: 3, c: 4,
  },
  violations: [
    {
      constraint: {
        fields: {
          a: { occurs: range::[0, 1] },
          b: { occurs: range::[1, 1] },
          c: { occurs: range::[1, 3] },
        },
      },
      code: fields_mismatch,
      children: [
        {
          fieldName: "a",
          value: { a: 1, a: 2 },
          violations: [
            {
              constraint: { occurs: range::[0, 1] },
              code: occurs_mismatch,
            }
          ],
        },
        {
          fieldName: "b",
          violations: [
            {
              constraint: { occurs: range::[1, 1] },
              code: occurs_mismatch,
            }
          ],
        },
        {
          fieldName: "c",
          value: { c: 1, c: 2, c: 3, c: 4 },
          violations: [
            {
              constraint: { occurs: range::[1, 3] },
              code: occurs_mismatch,
            }
          ],
        },
      ],
    },
  ],
}

