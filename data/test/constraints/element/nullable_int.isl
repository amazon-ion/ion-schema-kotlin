type::{
  element: nullable::int,
}
valid::[
  [],
  [1],
  [1, null.int, 3],
  [1, null, 3],
  (),
  (1),
  (1 null.int 3),
  { a: 1, b: null.int, c: null },
  document::"1 null.int",
  document::"1 null",
]
invalid::[
  null.list,
  [1.],
  [1e0],
  (1 2 3 true 4),
  (null.string),
  { a: 1, b: null.string },
  document::"1.",
  document::"null.symbol",
]

