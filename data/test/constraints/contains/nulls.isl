type::{
  contains: [null.null, null.bool, null.int, null.string],
}
valid::[
  [null.int, null.null, null.bool, null.string],
]
invalid::[
  null,
  null.null,
  null.bool,
  null.string,
  [],
  (),
]

type::{
  contains: [null, null.null],
}
valid::[
  [null],
  (null),
]
invalid::[
  [],
  (),
]
