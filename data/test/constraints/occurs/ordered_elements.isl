// default 'occurs' behavior for each ordered_elements type is:
//   occurs: 1
//
type::{
  ordered_elements: [
    bool,
    { type: int, occurs: optional },
    { type: decimal, occurs: required },
    { type: symbol, occurs: range::[0, 3] },
  ],
}
valid::[
  [true, 3.0],
  [true, 3.0, a],
  [true, 3.0, a, b],
  [true, 3.0, a, b, c],
  (true 2 3.0),
  (true 2 3.0 a),
  (true 2 3.0 a b),
  (true 2 3.0 a b c),
  document::"true 2 3.0",
  document::"true 2 3.0 a",
  document::"true 2 3.0 a b",
  document::"true 2 3.0 a b c",
]
invalid::[
  [true, false],
  [true, 2, 3],
  [true, 2, 3.0, 4.0],
  [true, 2, 3.0, true],
  [true, 2, 3.0, a, b, c, d],
  document::"true 2 3.0 a b c d",
]

