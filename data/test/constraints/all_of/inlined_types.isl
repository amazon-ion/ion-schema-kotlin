type::{
  all_of: [
    { codepoint_length: range::[1, 4] },
    { codepoint_length: range::[3, 4] },
    { codepoint_length: range::[3, 5] },
  ],
}
valid::[
  abc,
  abcd,
  "abc",
  "abcd",
]
invalid::[
  '',
  a,
  ab,
  abcde,
  abcdef,
  "",
  "a",
  "ab",
  "abcde",
  "abcdef",
]

