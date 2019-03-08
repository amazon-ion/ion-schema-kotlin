type::{
  ordered_elements: [
    { type: bool, occurs: required },
    { type: int, occurs: optional },
    { type: decimal, occurs: optional },
    { type: float, occurs: optional },
    { type: symbol, occurs: required },
    { type: bool, occurs: required },
  ],
}
valid::[
  (true hello false),
  document::"true hello false",
]
invalid::[
  (true hello),
  (true {}),
  (true {} true),
  document::"true hello",
]

