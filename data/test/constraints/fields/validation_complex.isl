schema_header::{}
type::{
  name: ShortString,
  codepoint_length: range::[0, 10],
  type: string,
}
type::{
  name: Book,
  type: struct,
  fields: {
    pretitle: string,
    title: ShortString,
    subtitle: { type: string, codepoint_length: 5 },
    subsubtitle: type::{ type: string, codepoint_length: 5 },
  }
}
type::{
  name: Person,
  type: struct,
  fields: {
    favoriteBook: Book,
  }
}
schema_footer::{}

test_validation::{
  type: Person,
  value: {
    favoriteBook: {
      pretitle: blah,
      title: "The Title of this Book is Too Long",
      subtitle: "abc",
      subsubtitle: "xyz",
    },
  },
  violations: [
    {
      constraint: { fields: { favoriteBook: Book } },
      code: fields_mismatch,
      children: [
        {
          fieldName: "favoriteBook",
          value: {
            pretitle: blah,
            title: "The Title of this Book is Too Long",
            subtitle: "abc",
            subsubtitle: "xyz",
          },
          violations: [
            {
              constraint: { type: Book },
              code: type_mismatch,
              violations: [
                {
                  constraint: {
                    fields: {
                      pretitle: string,
                      title: ShortString,
                      subtitle: { type: string, codepoint_length: 5 },
                      subsubtitle: type::{ type: string, codepoint_length: 5 },
                    },
                  },
                  code: fields_mismatch,
                  children: [
                    {
                      fieldName: "pretitle",
                      value: blah,
                      violations: [
                        {
                          constraint: { type: string },
                          code: type_mismatch,
                        },
                      ],
                    },
                    {
                      fieldName: "title",
                      value: "The Title of this Book is Too Long",
                      violations: [
                        {
                          constraint: { type: ShortString },
                          code: type_mismatch,
                          violations:[
                            {
                              constraint: { codepoint_length: range::[0, 10] },
                              code: invalid_codepoint_length,
                            },
                          ],
                        },
                      ],
                    },
                    {
                      fieldName: "subtitle",
                      value: "abc",
                      violations: [
                        {
                          constraint: { codepoint_length: 5 },
                          code: invalid_codepoint_length,
                        },
                      ],
                    },
                    {
                      fieldName: "subsubtitle",
                      value: "xyz",
                      violations: [
                        {
                          constraint: { codepoint_length: 5 },
                          code: invalid_codepoint_length,
                        },
                      ],
                    },
                  ],
                },
              ],
            },
          ],
        },
      ],
    },
  ],
}

