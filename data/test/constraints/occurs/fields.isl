// default behavior is:
//   occurs: optional
//
type::{ fields: { a: string } }
valid::[
  { },
  { a: "hi" },
  { other_field: null },
]
invalid::[
  null,
  null.struct,
  { a: null },
  { a: null.string },
  { a: 5 },
]

type::{ fields: { a: { type: string, occurs: required } } }
valid::[
  { a: "hi" },
]
invalid::[
  null,
  { },
  { a: null },
  { a: null.string },
  { a: 5 },
  { a: null.int },
]

type::{ fields: { a: { type: string, occurs: 3 } } }
valid::[
  { a: "1", a: "2", a: "3" },
]
invalid::[
  null,
  { },
  { a: null, a: null, a: null },
  { a: null.int, a: null.int, a: null.int },
  { a: null.string, a: null.string, a: null.string },
  { a: "1", a: "2" },
  { a: "1", a: "2", a: "3", a: "4" },
]

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

invalid_type::{ fields: { a: { occurs: 0 } } }
invalid_type::{ fields: { a: { occurs: range::[0, 0] } } }
invalid_type::{ fields: { a: { occurs: range::[1, 0] } } }
invalid_type::{ fields: { a: { occurs: range::[-1, 1] } } }
invalid_type::{ fields: { a: { occurs: range::[-2, -1] } } }
invalid_type::{ fields: { a: { occurs: range::[exclusive::1, exclusive::1] } } }
invalid_type::{ fields: { a: { occurs: range::[1, exclusive::2] } } }
invalid_type::{ fields: { a: { occurs: range::[exclusive::1, 2] } } }
