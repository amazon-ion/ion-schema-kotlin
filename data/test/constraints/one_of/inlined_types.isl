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

