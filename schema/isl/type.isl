schema_header::{
  imports: [
    { id: "isl/annotations.isl",         type: annotations },
    { id: "isl/byte_length.isl",         type: byte_length },
    { id: "isl/codepoint_length.isl",    type: codepoint_length },
    { id: "isl/container_length.isl",    type: container_length },
    { id: "isl/contains.isl",            type: contains },
    { id: "isl/content.isl",             type: content },
    { id: "isl/occurs.isl",              type: occurs },
    { id: "isl/precision.isl",           type: precision },
    { id: "isl/regex.isl",               type: regex },
    { id: "isl/scale.isl",               type: scale },
    { id: "isl/timestamp_offset.isl",    type: timestamp_offset },
    { id: "isl/timestamp_precision.isl", type: timestamp_precision },
    { id: "isl/valid_values.isl",        type: valid_values },
  ],
}

type::{
  name: type_name,
  type: symbol,
}

type::{
  name: type_inline,
  type: struct,
  fields: {
    all_of:              list_of_type_references,
    annotations:         annotations,
    any_of:              list_of_type_references,
    byte_length:         byte_length,
    codepoint_length:    codepoint_length,
    container_length:    container_length,
    contains:            contains,
    content:             content,
    element:             type_reference,
    fields:              { type: struct, element: type_reference },
    not:                 type_reference,
    occurs:              occurs,
    one_of:              list_of_type_references,
    ordered_elements:    list_of_type_references,
    precision:           precision,
    regex:               regex,
    scale:               scale,
    timestamp_offset:    timestamp_offset,
    timestamp_precision: timestamp_precision,
    type:                type_reference,
    valid_values:        valid_values,
  },
}

type::{
  name: type,
  type: type_inline,
  annotations: required::[type],
  fields: {
    name: symbol,
  },
}

type::{
  name: type_import_inline,
  type: struct,
  fields: {
    id: { type: string, occurs: required },
    type: { type: symbol, occurs: required },
  },
  annotations: [nullable],
}

type::{
  name: type_reference,
  any_of: [
    type_name,
    type_inline,
    type_import_inline,
  ],
  annotations: [nullable],
}

type::{
  name: list_of_type_references,
  type: list,
  element: type_reference,
}

schema_footer::{
}

