$ion_schema_1_0

"Open content should be preserved!"

schema_header::{
  // A comment that should be preserved!
  imports: [
    {id:"floats.isl",type:positive_float},
    {id:"ints.isl",type:negative_int},
    {id:"ints.isl",type:positive_int},
  ],
  open_content: "Open content should be preserved!",
}

type::{
  name: Foo,
  any_of: [
    positive_int,
    positive_float,
    negative_int,
  ],
  open_content: "Open content should be preserved!",
}

schema_footer::{
  open_content: "Open content should be preserved!",
}
