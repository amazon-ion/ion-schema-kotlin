type::{
  container_length: range::[1, max],
}
valid::[
  [1],
  [1, 2],
  (null),
  (null null),
  { a: 1 },
  { a: 1, b: 2 },
]
invalid::[
  null,
  null.bool,
  null.null,
  null.list,
  null.sexp,
  null.struct,
  [],
  (),
  {},
  5,
]

type::{
  container_length: min::2,
}
valid::[
  { a: 1, a: 1 },
]
