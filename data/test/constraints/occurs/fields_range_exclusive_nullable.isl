type::{ fields: { a: { type: nullable::string, occurs: range::[exclusive::1, exclusive::3] } } }
valid::[
  { a: null, a: null },
  { a: null.string, a: null.string },
  { a: "1", a: "2" },
]
invalid::[
  null,
  { },
  { a: null },
  { a: null.string },
  { a: null, a: null, a: null },
  { a: null.string, a: null.string, a: null.string },
  { a: "1" },
  { a: "1", a: "2", a: "3" },
]

