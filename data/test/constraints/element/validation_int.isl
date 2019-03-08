type::{
  element: int,
  type: $any,
}

test_validation::{
  value: hi,
  violations: [
    { constraint: { element: int }, code: invalid_type },
  ],
}

test_validation::{
  values: (null.list null.sexp null.struct),
  violations: [
    { constraint: { element: int }, code: null_value },
  ],
}

test_validation::{
  values: (("a" 5 b) ["a", 5, b] document::'''"a" 5 b'''),
  violations: [
    {
      constraint: { element: int },
      code: element_mismatch,
      children: [
        {
          index: 0,
          value: "a",
          violations: [
            { constraint: { type: int }, code: type_mismatch },
          ],
        },
        {
          index: 2,
          value: b,
          violations: [
            { constraint: { type: int }, code: type_mismatch },
          ],
        },
      ],
    },
  ],
}

test_validation::{
  value: {a: "a", b: 5, c: b},
  violations: [
    {
      constraint: { element: int },
      code: element_mismatch,
      children: [
        {
          fieldName: "a",
          value: "a",
          violations: [
            { constraint: { type: int }, code: type_mismatch },
          ],
        },
        {
          fieldName: "c",
          value: b,
          violations: [
            { constraint: { type: int }, code: type_mismatch },
          ],
        },
      ],
    },
  ],
}

