schema_header::{
  imports: [
    { id: "schema/util/positive_int.isl", type: positive_int }
  ],
}
type::{
  name: import_type_test,
  element: positive_int,
}
schema_footer::{
}

valid::{
  import_type_test: [
    [1, 2, 3],
    (1 2 3),
    { a: 1, b: 2, c: 3 },
    document::"1 2 3",
  ],
}

invalid::{
  import_type_test: [
    [0],
    (-1),
    { a: 0.0 },
    document::"0",
    document::"hi",
  ],
}

