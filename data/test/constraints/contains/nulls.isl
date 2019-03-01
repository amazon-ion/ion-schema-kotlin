type::{
  contains: [null.null, null.bool, null.int, null.string],
}
valid::[
  [null.int, null.null, null.bool, null.string],
  document::"null.bool null.null null.string null.int",
]
invalid::[
  null,
  null.null,
  null.bool,
  null.string,
  [],
  (),
  document::"null.int null.null null.string",
]

type::{
  contains: [null, null.null],
}
valid::[
  [null],
  (null),
  document::"null",
]
invalid::[
  [],
  (),
  document::"",
]

