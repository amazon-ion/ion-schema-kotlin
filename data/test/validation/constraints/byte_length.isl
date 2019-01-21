type::{
  byte_length: range::[6, max],
  type: $any,
}

test_validation::{
  values: [null.blob, null.clob],
  violations: [
    { constraint: { byte_length: range::[6, max] }, code: null_value },
  ],
}

test_validation::{
  value: hello,
  violations: [
    { constraint: { byte_length: range::[6, max] }, code: invalid_type },
  ],
}

test_validation::{
  values: [{{aGVsbG8=}}, {{"hello"}}],
  violations: [
    { constraint: { byte_length: range::[6, max] }, code: invalid_byte_length },
  ],
}

