// duplicate import:
invalid_schema::document::'''
  schema_header::{
    imports: [
      { id: "schema/util/positive_int.isl" },
      { id: "schema/util/positive_int.isl" },
    ],
  }
  schema_footer::{}
'''

