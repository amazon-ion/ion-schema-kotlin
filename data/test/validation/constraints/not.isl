type::{
  type: struct,
  fields: {
    one: { not: string },
  },
}
test_validation::{
  value: { one: "hi" },
  violations: [
    {
      constraint: { fields: { one: { not: string } } },
      code: fields_mismatch,
      children: [
        {
          fieldName: "one",
          value: "hi",
          violations: [
            {
              constraint: { not: string },
              code: type_matched,
            }
          ],
        }
      ],
    },
  ],
}
