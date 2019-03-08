type::{ fields: { a: { type: $string, occurs: 3 } } }
valid::[
  { a: "1", a: "2", a: "3" },
  { a: null.string, a: null.string, a: null.string },
]
invalid::[
  null,
  { },
  { a: null, a: null, a: null },
  { a: null.int, a: null.int, a: null.int },
  { a: "1", a: "2" },
  { a: "1", a: "2", a: "3", a: "4" },
]

