schema_header::{}

// an int (>= 0) with optional 'exclusive' annotation
type::{
  name: int_non_negative,
  type: int,
  annotations: [exclusive],
  valid_values: range::[0, max],
}

// range::[ <INT>, <INT> ]
// range::[ min, <INT> ]
// range::[ <INT>, max ]
// range::[ min, max ]
type::{
  name: range_int_non_negative,
  type: list,
  annotations: required::[range],
  ordered_elements: [
    { one_of: [ int_non_negative, { valid_values: [min] } ] },
    { one_of: [ int_non_negative, { valid_values: [max] } ] },
  ],
  container_length: 2,
}

// byte_length: <INT>
// byte_length: <RANGE<INT>>
type::{
  name: byte_length,
  fields: {
    byte_length: {
      one_of: [
        int_non_negative,         // <INT>
        range_int_non_negative,   // <RANGE<INT>>
      ],
      occurs: required,
    },
  },
}

schema_footer::{}


valid::{
  int_non_negative: [
    0,
    1,
    exclusive::100,
  ],
  range_int_non_negative: [
    range::[0, 0],
    range::[exclusive::0, exclusive::0],
    range::[min, max],
  ],
  byte_length: [
    { byte_length: 0 },
    { byte_length: 5 },
    { byte_length: range::[0, 0] },
    { byte_length: range::[exclusive::0, exclusive::0] },
    { byte_length: range::[min, max] },
  ],
}

invalid::{
  int_non_negative: [
    -1,
    0.5,
    hello,
  ],
  range_int_non_negative: [
    [0, 0],
    range::[-1, 0],
    range::[0, -1],
    range::[0],
    range::[0, 1, 2],
  ],
  byte_length: [
    { byte_length: -1 },
    { byte_length: 0.5 },
    { byte_length: hello },
    { byte_length: [0, 0] },
    { byte_length: range::[-1, 0] },
    { byte_length: range::[0, -1] },
    { byte_length: range::[0] },
    { byte_length: range::[0, 1, 2] },
    { },
  ],
}
