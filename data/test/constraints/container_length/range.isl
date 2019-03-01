type::{
  container_length: range::[1, 3],
}
valid::[
  [1],
  [1, 2],
  [1, 2, 3],
  (a),
  (a b),
  (a b c),
  { a: 1 },
  { a: 1, b: 2 },
  { a: 1, b: 2, c: 3 },
  document::"1",
  document::"1 2",
  document::"1 2 3",
]
invalid::[
  null,
  null.bool,
  null.null,
  null.list,
  null.sexp,
  null.struct,
  [],
  [1, 2, 3, 4],
  (),
  (1 2 3 4),
  {},
  { a: 1, b: 2, c: 3, d: 4 },
  document::"",
  document::"1 2 3 4",
]
