type::{
  ordered_elements: [int, string, { type: symbol }],
}
valid::[
  [0, "a", b],
  (1  "b"  c),
  document::"2 \"a\" b",
]
invalid::[
  null,
  null.null,
  null.bool,
  null.list,
  null.sexp,
  [1],
  [1, "a"],
  [1, "a", "b"],
  ["a", "b", c],
  [1, "a", b, null],
  document::'''1 "a" b null''',
]

type::{
  ordered_elements: [],
}
valid::[
  [],
  (),
  document::"",
]
invalid::[
  [a],
  (a),
  document::"a",
]

type::{
  ordered_elements: [
    {},                // equivalent to { type: any }
  ],
}
valid::[
  [5],
]
invalid::[
  [null],
]

