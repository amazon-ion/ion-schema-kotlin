type::{
  regex: "\0\a\b\t\n\f\r\v",
}
valid::[
  '\0\a\b\t\n\f\r\v',
  "\0\a\b\t\n\f\r\v",
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

