// type 'positive_int' should not be recognized in this schema:
invalid_schema::document::'''
  schema_header::{
    imports: [
      { id: "schema/import/import_type_by_alias.isl" },
    ],
  }
  type::{
    name: import_schema_with_aliased_type_test,
    type: positive_int,
  }
  schema_footer::{}
'''
