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

