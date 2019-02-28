schema_header::{
}

type::{
  name: range_boundary_number,
  type: number,
  annotations: [exclusive],
}

type::{
  name: range_number,
  type: list,
  annotations: required::[range],
  ordered_elements: [
    { one_of: [ range_boundary_number, { valid_values: [min] } ] },
    { one_of: [ range_boundary_number, { valid_values: [max] } ] },
  ],
  container_length: 2,
}

type::{
  name: range_boundary_timestamp,
  type: timestamp,
  annotations: [exclusive],
}

type::{
  name: range_timestamp,
  type: list,
  annotations: required::[range],
  ordered_elements: [
    { one_of: [ range_boundary_timestamp, { valid_values: [min] } ] },
    { one_of: [ range_boundary_timestamp, { valid_values: [max] } ] },
  ],
  container_length: 2,
}

type::{
  name: valid_values,
  type: list,
  any_of: [
    { type: list, element: $any },
    range_number,
    range_timestamp,
  ],
}

schema_footer::{
}

