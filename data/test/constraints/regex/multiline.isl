type::{
  regex: m::"^hello world$",
}
valid::[
  'hello world',
  'hello world\n\n\n',
  '\n\n\nhello world',
  '\n\n\nhello world\n\n\n',
  '\r\r\rhello world\r\r\r',

  "hello world",
  "hello world\n\n\n",
  "\n\n\nhello world",
  "\n\n\nhello world\n\n\n",
  "\r\r\rhello world\r\r\r",
]
invalid::[
  null,
  null.null,
  null.string,
  null.symbol,
  null.bool,
  " hello world",
  "hello world ",
  "\n hello world",
  "hello world \n",
  "hello\n world",
  "hello \nworld",
  "hello\r world",
  "hello \rworld",
]

