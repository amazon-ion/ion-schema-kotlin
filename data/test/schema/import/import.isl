schema_header::{
  imports: [
    { id: "schema/import/abcde.isl" },
  ],
}
type::{
  name: import_test,
  ordered_elements: [ a, b, c, d, e ],
}
schema_footer::{}

valid::{
  import_test: [
    [a, "b", c, "d", e],
    ("a" b "c" d "e"),
    document::'''a b c d e''',
  ],
  a: [a, "a"],
  b: [b, "b"],
  c: [c, "c"],
  d: [d, "d"],
  e: [e, "e"],
}
invalid::{
  import_test: [
    [a, b, c, d],
    [a, b, c, d, e, f],
  ],
  a: [   "b", c, "d", e],
  b: [a,      c, "d", e],
  c: [a, "b",    "d", e],
  d: [a, "b", c,      e],
  e: [a, "b", c, "d",  ],
}

