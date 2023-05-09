$ion_schema_1_0

"Open content should be preserved!"

type::{
  name: positive_int,
  type: int,
  valid_values: range::[1, max],
  open_content: "Open content should be preserved!",
}

type::{
  name: negative_int,
  type: int,
  valid_values: range::[min, -1],
  open_content: "Open content should be preserved!",
}
