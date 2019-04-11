type::{
  regex: "hello\tworld",
}
valid::[
  'hello\tworld',
  'hello	world',
  "hello\tworld",
  "hello	world",
]
invalid::[
  null,
  null.null,
  null.string,
  null.symbol,
  null.bool,
  'hello world',
  "hello world",
]

type::{
  regex: "hello	world",
}
valid::[
  'hello\tworld',
  'hello	world',
  "hello\tworld",
  "hello	world",
]
invalid::[
  null,
  null.null,
  null.string,
  null.symbol,
  null.bool,
  'hello world',
  "hello world",
]

