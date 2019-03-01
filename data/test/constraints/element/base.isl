type::{
  element: int,
}
valid::[
  [],
  [1],
  [1, 2, 3],
  (),
  (1),
  (1 2 3),
  { a: 1, b: 2, c: 3 },
  document::"1 2 3",
]
invalid::[
  null.list,
  [1.],
  [1e0],
  [1, 2, null.int],
  (1 2 3 true 4),
  { a: 1, b: 2, c: true },
  { a: 1, b: 2, c: null.int },
  document::"1.",
  document::"1 2 null.int",
  document::"1 2 true",
]

