type::{
  type: struct,   // open content
  fields: {
    a: string,
    b: int,
    c: symbol,
  },
}
valid::[
  { },
  { a: "hi" },
  { a: "hi", b: 1 },
  { a: "hi", b: 1, c: hi },
  {          b: 1, c: hi },
  {                c: hi },
  { a: "hi",       c: hi },
  { x: 0 },
  { x: 1, a: "hi", b: 1, c: hi, z: 2 },
]


type::{
  type: struct,
  content: closed,
  fields: {
    a: string,
    b: int,
    c: symbol,
  },
}
valid::[
  { },
  { a: "hi" },
  { a: "hi", b: 1 },
  { a: "hi", b: 1, c: hi },
  {          b: 1, c: hi },
  {                c: hi },
  { a: "hi",       c: hi },
]
invalid::[
  { x: 0 },
  { w: 0, a: "hi", x: 1, b: 1, y: 2, c: hi, z: 3, z: 4 },
]


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

