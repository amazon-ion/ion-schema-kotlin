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
]
invalid::[
  [1.],
  [1e0],
  (1 2 3 true 4),
]
