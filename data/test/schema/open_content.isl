// verify that open content in a schema is ignored
schema_header::{
  open: content,
}
not_a_type::{}
5
type::{
  name: short_string,
  codepoint_length: range::[1, 5],
}
hello
also_not_a_type::{}
schema_footer::{
  open: content,
}

valid::{
  short_string: [
    "a",
    "abcde",
  ],
}
invalid::{
  short_string: [
    "",
    "abcdef",
  ],
}

