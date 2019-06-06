type::{
  regex: "\\.\\^\\$\\|\\?\\*\\+\\[\\]\\(\\)\\{\\}\\\\",
}
valid::[
  '.^$|?*+[](){}\\',
  ".^$|?*+[](){}\\",
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

