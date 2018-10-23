// TBD...
type::{
  regex: "hello",
}
valid::[
  hello,
  "hello",
  "I can't wait to say 'hello' to you!",
  "I can't wait to say \"hello\" to you!",
  'I can\'t wait to say \'hello\' to you!',
  'I can\'t wait to say "hello" to you!',
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
  'goodbye',
]
