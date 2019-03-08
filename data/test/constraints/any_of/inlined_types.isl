type::{
  any_of: [
    { codepoint_length: 3 },
    { codepoint_length: range::[2, 4] },
    { codepoint_length: 5 },
  ],
}
valid::[
  ab,
  abc,
  abcd,
  abcde,
  "ab",
  "abc",
  "abcd",
  "abcde",
]
invalid::[
  '',
  a,
  abcdef,
  "",
  "a",
  "abcdef",
]

