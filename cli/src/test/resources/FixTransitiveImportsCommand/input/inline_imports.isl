$ion_schema_1_0

schema_header:: {
  // A comment that should be preserved!
  open_content: "Open content should be preserved!",
}

type::{
  // A comment that should be preserved!
  name: foo,
  // Another comment that should be preserved!
  open_content: "Open content should be preserved!",
  any_of: [
    nullable::{ id: "floats.isl", type: positive_int },
    { id: "floats.isl", type: negative_int },
    { id: "floats.isl", type: positive_float },
  ],
}

schema_footer::{
  // A comment that should be preserved!
  open_content: "Open content should be preserved!",
}
