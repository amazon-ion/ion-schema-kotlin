schema_header::{
}

type::{
  name: Customer,
  type: struct,
  fields: {
    firstName: { type: string, occurs: required },
    middleName: string,
    lastName: { type: string, occurs: required },
  }
}

schema_footer::{
}

valid::{
  Customer: [
    { firstName: "Phil", lastName: "Collins" },
    { firstName: "Phil", lastName: "Collins", middleName: "Billy" },
  ],
}

invalid::{
  Customer: [
    { firstName: "Phil", middleName: "Billy" },
  ],
}
