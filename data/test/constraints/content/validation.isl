type::{
  type: struct,
  content: closed,
  fields: {
    a: string,
    b: int,
    c: symbol,
  },
}

test_validation::{
  value: { w: 0, a: "hi", x: 1, b: 10, y: 2, c: hi, z: 3, z: 4 },
  violations: [
    {
      constraint: { content: closed },
      code: unexpected_content,
      children: [
        { fieldName: "w", value: 0 },
        { fieldName: "x", value: 1 },
        { fieldName: "y", value: 2 },
        { fieldName: "z", value: 3 },
        { fieldName: "z", value: 4 },
      ],
    },
  ],
}


type::{
  type: struct,
  content: closed,
  fields: {                   // closed
    a: symbol,
    b: type::{
      type: struct,
      fields: {               // open
        x: symbol,
      },
    },
    c: type::{                // closed
      type: struct,
      content: closed,
      fields: {
        y: symbol,
      },
    },
  },
}
test_validation::{
  value: { c: { z: z }, d: d },
  violations: [
    {
      constraint: { content: closed },
      code: unexpected_content,
      children: [
        { fieldName: "d", value: d },
      ],
    },
    {
      constraint: {
        fields: {
          a: symbol,
          b: type::{
            type: struct,
            fields: {
              x: symbol,
            },
          },
          c: type::{
            type: struct,
            content: closed,
            fields: {
              y: symbol,
            },
          },
        },
      },
      code: fields_mismatch,
      children: [
        {
          fieldName: "c",
          value: { z: z },
          violations: [
            {
              constraint: { content: closed },
              code: unexpected_content,
              children: [
                { fieldName: "z", value: z },
              ],
            },
          ],
        },
      ],
    },
  ],
}

