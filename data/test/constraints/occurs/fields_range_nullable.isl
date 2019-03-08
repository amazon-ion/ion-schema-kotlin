type::{ fields: { a: { type: nullable::string, occurs: range::[1, 3] } } }
valid::[
  { a: null },
  { a: null, a: null },
  { a: null, a: null, a: null },
  { a: null.string },
  { a: null.string, a: null.string },
  { a: null.string, a: null.string, a: null.string },
  { a: "1" },
  { a: "1", a: "2" },
  { a: "1", a: "2", a: "3" },
]
invalid::[
  null,
  { },
  { a: null, a: null, a: null, a: null },
  { a: null.string, a: null.string, a: null.string, a: null.string },
  { a: "1", a: "2", a: "3", a: "4" },
]

