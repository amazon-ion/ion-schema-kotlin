schema_header::{
  imports: [
    { id: "isl/non_negative_int.isl" },
  ],
}

type::{
  name: occurs,
  one_of: [
    non_negative_int,
    range_non_negative_int,
    { valid_values: [optional, required] },
  ],
}

schema_footer::{
}

