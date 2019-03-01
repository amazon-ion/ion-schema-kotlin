type::{
  type: document,
  ordered_elements: [
    bool,
    symbol,
    int,
  ],
}
valid::[
  document::"true hello 5",
]
invalid::[
  null,
  null.symbol,
  [true, hello, 5],
  document::"true hello 5.0",
]

invalid_type::{
  type: nullable::document,
}

invalid_type::{
  type: nullable::{ type: document },
}

