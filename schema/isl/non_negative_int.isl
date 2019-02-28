type::{
  name: non_negative_int,
  type: int,
  valid_values: range::[0, max],
}

type::{
  name: range_boundary_non_negative_int,
  type: non_negative_int,
  annotations: [exclusive],
}

type::{
  name: range_non_negative_int,
  type: list,
  annotations: required::[range],
  ordered_elements: [
    { one_of: [ range_boundary_non_negative_int, { valid_values: [min] } ] },
    { one_of: [ range_boundary_non_negative_int, { valid_values: [max] } ] },
  ],
  container_length: 2,
}

