schema_header::{
}

type::{
  name: positive_int,
  type: int,
  valid_values: range::[1, max],
}

type::{
  name: range_boundary_positive_int,
  type: positive_int,
  annotations: [exclusive],
}

type::{
  name: range_positive_int,
  type: list,
  annotations: required::[range],
  ordered_elements: [
    { one_of: [ range_boundary_positive_int, { valid_values: [min] } ] },
    { one_of: [ range_boundary_positive_int, { valid_values: [max] } ] },
  ],
  container_length: 2,
}

type::{
  name: range_or_exact_positive_int,
  one_of: [
    positive_int,
    range_positive_int,
  ],
}

type::{
  name: precision,
  type: range_or_exact_positive_int,
}

schema_footer::{
}

