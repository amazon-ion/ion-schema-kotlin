$ion_schema_1_0

"Open content should be preserved!"

schema_header::{
  // A comment that should be preserved!
  // In the "unfixed" version, this file transitively exports positive_int and negative_int
  imports: [],
  open_content: "Open content should be preserved!",
}

type::{
  name: positive_float,
  type: float,
  valid_values: [range::[exclusive::0, max], +inf],
  open_content: "Open content should be preserved!",
}

type::{
  name: negative_float,
  type: float,
  valid_values: [range::[min, exclusive::0], -inf],
  open_content: "Open content should be preserved!",
}

schema_footer::{
  open_content: "Open content should be preserved!",
}
