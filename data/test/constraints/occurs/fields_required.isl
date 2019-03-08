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

