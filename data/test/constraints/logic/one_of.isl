type::{
  one_of: [
    bool,
    int,
    string,
  ],
}
valid::[
  true,
  5,
  "hi",
]
invalid::[
  null,
  5.0,
  hi,
]

type::{
  one_of: [
    string,
    symbol,
    any,
  ],
}
valid::[
  true,
  5,
]
invalid::[
  "hi",
  hi,
]

type::{
  one_of: [
    { codepoint_length: 3 },
    { codepoint_length: range::[2, 4] },
    { codepoint_length: 5 },
  ],
}
valid::[
  ab,
  abcd,
  abcde,
]
invalid::[
  "",
  abc,
  abcdef,
]
