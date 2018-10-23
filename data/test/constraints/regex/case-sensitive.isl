type::{
  regex: "hello",
}
valid::[
  hello,
  "hello",
]
invalid::[
  null,
  null.null,
  null.string,
  null.symbol,
  null.bool,
  '',
  "",
  HellO,
  "HELLO",
]
