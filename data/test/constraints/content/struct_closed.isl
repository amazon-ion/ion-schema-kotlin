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

