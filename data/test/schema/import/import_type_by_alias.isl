schema_header::{
  imports: [
    { id: "schema/util/positive_int.isl", type: positive_int, as: positive_int_1 },
    { id: "schema/util/positive_int.isl", type: positive_int, as: positive_int_2 },
  ],
}
type::{
  name: import_type_by_alias_test,
  ordered_elements: [
    positive_int_1,
    positive_int_2,
  ],
}
schema_footer::{
}

valid::{
  import_type_by_alias_test: [
    [1, 2],
    (1 2),
    document::"1 2",
  ],
}

invalid::{
  import_type_by_alias_test: [
    [0, 0],
    (-1 -1),
    document::"0 0",
    document::"1 hi",
  ],
}

