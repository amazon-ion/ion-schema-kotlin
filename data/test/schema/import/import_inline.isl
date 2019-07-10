// verifies that inline type imports work and don't cause duplicate name conflicts
// with other inline imports or schema header imports
schema_header::{
  imports: [
    { id: "schema/import/import_inline_a1.isl", type: a },
    { id: "schema/import/import_inline_a1.isl", type: a, as: b },
  ],
}
type::{
  name: import_inline_test,
  type: struct,
  fields: {
    a1: a,
    a2: { id: "schema/import/import_inline_a2.isl", type: a },
    a3: { id: "schema/import/import_inline_a3.isl", type: a },
    b1: b,
    b2: { id: "schema/import/import_inline_a2.isl", type: a, as: b },
    b3: { id: "schema/import/import_inline_a3.isl", type: a, as: b },
  },
}
schema_footer::{}

valid::{
  import_inline_test: [
    { a1: 1, a2: 2, a3: 3, b: 1, b2: 2, b3: 3 },
  ],
}
invalid::{
  import_inline_test: [
    { a1: 0, a2: 2, a3: 3, b1: 1, b2: 2, b3: 3 },
    { a1: 1, a2: 0, a3: 3, b1: 1, b2: 2, b3: 3 },
    { a1: 1, a2: 2, a3: 0, b1: 1, b2: 2, b3: 3 },
    { a1: 1, a2: 2, a3: 3, b1: 0, b2: 2, b3: 3 },
    { a1: 1, a2: 2, a3: 3, b1: 1, b2: 0, b3: 3 },
    { a1: 1, a2: 2, a3: 3, b1: 1, b2: 2, b3: 0 },
  ],
}

