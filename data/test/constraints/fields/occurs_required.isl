type::{
  fields: {
    a: { type: int, occurs: required },
    b: int,
  },
}
valid::[
  { a: 1 },
  { a: 1, b: 2 },
  { a: 1, b: 2, c: 3 },
]
invalid::[
  null,
  null.struct,
  {},
  { b: 2 },
  5,
  [],
  (),
]

