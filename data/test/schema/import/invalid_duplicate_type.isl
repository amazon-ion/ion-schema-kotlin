// duplicate type definition:
invalid_schema::document::'''
  schema_header::{
    imports: [
      { id: "schema/util/positive_int.isl", type: positive_int },
    ],
  }
  type::{
    name: positive_int,
  }
  schema_footer::{}
'''

