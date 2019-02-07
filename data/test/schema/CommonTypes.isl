schema_header::{}

type::{
  name: positive_int,
  type: int,
  valid_values: range::[1, max]
}

schema_footer::{}

valid::{
  positive_int: [
    3,
    100,
  ],
}

invalid::{
  positive_int: [
    0,
    -1,
    0.5,
    hello,
  ],
}