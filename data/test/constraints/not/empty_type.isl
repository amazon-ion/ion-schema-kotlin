type::{
  not: {},     // equivalent to not: { type: any }
  type: $any,  // override the default "type: any" constraint, otherwise any null value will be invalid
}
valid::[
  null,
  null.blob,
  null.bool,
  null.clob,
  null.decimal,
  null.float,
  null.int,
  null.list,
  null.sexp,
  null.string,
  null.struct,
  null.symbol,
  null.timestamp,
]
invalid::[
  true,
  5,
  5e0,
  5d0,
  2019-01-01T,
  symbol,
  "string",
  {{ "clob" }},
  {{aGVsbG8=}},
  [],
  (),
  {},
]

