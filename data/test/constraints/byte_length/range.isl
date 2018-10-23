type::{
  byte_length: range::[5, 10],
}
valid::[
  {{"12345"}},
  {{"1234567890"}},
]
invalid::[
  null,
  null.bool,
  null.null,
  null.blob,
  null.clob,
  {{}},
  {{"1234"}},
  {{"12345678901"}},
]
