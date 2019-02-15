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


type::{
  ordered_elements: [
    { type: int, occurs: range::[2, 4] },
  ],
}

test_validation::{
  value: [ 1 ],
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


type::{
  ordered_elements: [
    { type: int, occurs: 2 },
    { type: symbol, occurs: range::[0, 1] },
    { type: string, occurs: range::[1, 3] },
  ],
}

test_validation::{
  value: [ 1, hello ],
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
  value: [ 1, 2, hello, "1", "2", "3", "4", "5" ],
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

