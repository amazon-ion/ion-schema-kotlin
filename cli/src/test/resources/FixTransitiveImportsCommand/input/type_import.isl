$ion_schema_1_0

"Open content should be preserved!"

schema_header::{
  // A comment that should be preserved!
  imports: [
    { id: "floats.isl", type: negative_float },
    { id: "floats.isl", type: positive_int },
    { id: "floats.isl", type: negative_int },
  ],
  open_content: "Open content should be preserved!",
}

type::{
  name: Foo,
  any_of: [
    positive_int,
    negative_float,
    negative_int,
  ],
  open_content: "Open content should be preserved!",
}

schema_footer::{
  open_content: "Open content should be preserved!",
}
