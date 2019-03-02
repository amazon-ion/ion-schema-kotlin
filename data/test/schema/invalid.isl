// missing schema_footer:
invalid_schema::document::'''
  schema_header::{}
'''

// missing schema_header:
invalid_schema::document::'''
  schema_footer::{}
'''

// type reference that can't be resolved:
invalid_schema::document::'''
  schema_header::{}
  type::{
    type: unknown_type,
  }
  schema_footer::{}
'''

