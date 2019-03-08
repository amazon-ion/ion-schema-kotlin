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

