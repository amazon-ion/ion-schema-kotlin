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

