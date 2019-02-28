schema_header::{
}

type::{
  name: timestamp_precision_value,
  type: symbol,
  valid_values: [
    year,
    month,
    day,
    minute,
    second,
    millisecond,
    microsecond,
    nanosecond,
  ],
}

type::{
  name: range_timestamp_precision_value,
  type: list,
  annotations: required::[range],
  ordered_elements: [
    { one_of: [ timestamp_precision_value, { valid_values: [min] } ] },
    { one_of: [ timestamp_precision_value, { valid_values: [max] } ] },
  ],
  container_length: 2,
}

type::{
  name: timestamp_precision,
  one_of: [
    timestamp_precision_value,
    range_timestamp_precision_value,
  ],
}

schema_footer::{
}

