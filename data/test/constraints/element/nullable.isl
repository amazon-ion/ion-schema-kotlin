type::{
  element: nullable::int,
}
valid::[
  // null.list, ?
  [],
  [1],
  [1, null.int, 3],
  [1, null, 3],
  (),
  (1),
  (1 null.int 3),
]
invalid::[
  [1.],
  [1e0],
  (1 2 3 true 4),
  (null.string),
]
