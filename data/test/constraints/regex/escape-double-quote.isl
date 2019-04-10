type::{
  regex: "\"free\"",
}
valid::[
  '"free"',
  "\"free\"",
  'Totally "free" food!',
  "Totally \"free\" food!",
]
invalid::[
  null,
  null.null,
  null.string,
  null.symbol,
  null.bool,
  '',
  "",
  'free',
  "free",
]

