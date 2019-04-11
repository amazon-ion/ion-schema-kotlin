type::{
  regex: "hello\bworld",
}
valid::[
  'hello\bworld',
  "hello\bworld",
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

