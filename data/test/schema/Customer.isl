schema_header::{
  imports: [
    { id: "schema/util/positive_int.isl", type: positive_int }
  ],
}
type::{
  name: Customer,
  type: struct,
  fields: {
    firstName: { type: string, occurs: required },
    middleName: string,
    lastName: { type: string, codepoint_length: range::[min, 7], occurs: required },
    age: { type: positive_int },
  }
}
schema_footer::{
}

valid::{
  Customer: [
    { firstName: "Phil", lastName: "Collins" },
    { firstName: "Phil", lastName: "Collins", middleName: "Billy" },
    { firstName: "Phil", lastName: "Collins", age: 68 },
  ],
}

invalid::{
  Customer: [
    { firstName: "Phil", middleName: "Billy" },
    { firstName: "Phil", lastName: "Collins", age: -1 },
  ],
}

