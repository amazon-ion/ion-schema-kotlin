type::{
  fields: {
    one: int,
  },
  type: $any,
}

test_validation::{
  value: null.struct,
  violations: [
    { constraint: { fields: { one: int } }, code: null_value },
  ],
}

test_validation::{
  value: 5,
  violations: [
    { constraint: { fields: { one: int } }, code: invalid_type },
  ],
}

test_validation::{
  value: { one: 1.0 },
  violations: [
    {
      constraint: { fields: { one: int } },
      code: fields_mismatch,
      children: [
        {
          fieldName: "one",
          value: 1.0,
          violations: [ { constraint: { type: int }, code: type_mismatch } ],
        },
      ],
    },
  ],
}

