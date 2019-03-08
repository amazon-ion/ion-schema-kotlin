type::{
  type: struct,
  fields: {
    one: {
      type: struct,
      fields: {
        two: int,
      },
    },
  },
}
test_validation::{
  value: { one: { two: 2.0 } },
  violations: [
    {
      constraint: { fields: { one: { type: struct, fields: { two: int } } } },
      code: fields_mismatch,
      children: [
        {
          fieldName: "one",
          value: { two: 2.0 },
          violations: [
            {
              constraint: { fields: { two: int } },
              code: fields_mismatch,
              children: [
                {
                  fieldName: "two",
                  value: 2.0,
                  violations: [
                    { constraint: { type: int }, code: type_mismatch },
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

