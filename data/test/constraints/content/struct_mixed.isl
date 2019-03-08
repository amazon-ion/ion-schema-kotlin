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
valid::[
  { },
  { a: a, b: { x: x, y: y, z: z }, c: { y: y } },
]
invalid::[
  { d: d },
  { c: { z: z } },
]

