type::{
  contains: [1, 2, 3],
}
valid::[
  [1, 2, 3],
  [4, 3, 2, 1],
  (2 3 1),
  (3 4 2 1),
  document::"1 2 3",
  document::"2 3 1 4",
]
invalid::[
  null,
  null.null,
  null.int,
  null.list,
  null.sexp,
  null.struct,
  [1, 2],
  [1, 2, 4],
  [1, 1, 1],
  (1 2),
  (1 2 4),
  (1 1 1),
  document::"1 1 1",
  document::"1 2 4",
]

