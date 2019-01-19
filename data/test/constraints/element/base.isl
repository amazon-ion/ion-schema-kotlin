type::{
  element: int,
}
valid::[
  // null.list, ?
  [],
  [1],
  [1, 2, 3],
  (),
  (1),
  (1 2 3),
  { a: 1, b: 2, c: 3 },
]
invalid::[
  [1.],
  [1e0],
  (1 2 3 true 4),
  { a: 1, b: 2, c: true },
]
