type::{
  contains: [null, null.bool, null.int, null.string],
}
valid::[
  [null.int, null, null.bool, null.string],
  document::"null.bool null null.string null.int",
]
invalid::[
  null,
  null.bool,
  null.string,
  [],
  (),
  (null.int null null.string),
  document::"null.int null null.string",
]

