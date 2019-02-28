schema_header::{
  imports: [
    { id: "isl/type.isl" },
  ],
}

type::{
  name: import,
  type: struct,
  fields: {
    id: { type: string, occurs: required },
    type: symbol,
    as: symbol,
  },
}

type::{
  name: schema_header,
  type: struct,
  annotations: required::[schema_header],
  fields: {
    imports: { type: list, element: import },
  },
}

type::{
  name: schema_footer,
  type: struct,
  annotations: required::[schema_footer],
}

type::{
  name: Schema,
  type: document,
  ordered_elements: [
    { type: schema_header, occurs: optional },
    { type: type_definition, occurs: range::[0, max] },
    { type: schema_footer, occurs: optional },
  ],
}

schema_footer::{
}

