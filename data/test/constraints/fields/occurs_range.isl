type::{
  fields: {
    c: { occurs: range::[2, 4] },
  },
}
valid::[
  { c: 1, c: a },
  { c: a, c: 2.0, c: "c" },
  { c: 1e0, c: 2d0, c: c, c: true },
]
invalid::[
  { c: 1 },
  { c: 1, c: 2, c: 3, c: 4, c: 5 },
  { a: 1, b: 2 },
  { a: 1, b: 2, c: 3 },
  { a: 1, b: 2, c: 3, d: 4 },
]

