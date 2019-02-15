type::{ type: int }

test_validation::{
  value: 7.0,
  violations: [
    { constraint: { type: int }, code: type_mismatch },
  ],
}

type::{
  type: any
}
test_validation::{
  value: null,
  violations: [
    {
      constraint: { type: any },
      code: type_mismatch,
      violations: [
        {
          constraint: { one_of: [blob, bool, clob, decimal, float, int, string, symbol, timestamp, list, sexp, struct] },
          code: no_types_matched,
          violations: [
            { constraint: { type: blob },      code: type_mismatch },
            { constraint: { type: bool },      code: type_mismatch },
            { constraint: { type: clob },      code: type_mismatch },
            { constraint: { type: decimal },   code: type_mismatch },
            { constraint: { type: float },     code: type_mismatch },
            { constraint: { type: int },       code: type_mismatch },
            { constraint: { type: string },    code: type_mismatch },
            { constraint: { type: symbol },    code: type_mismatch },
            { constraint: { type: timestamp }, code: type_mismatch },
            { constraint: { type: list },      code: type_mismatch },
            { constraint: { type: sexp },      code: type_mismatch },
            { constraint: { type: struct },    code: type_mismatch },
          ],
        },
      ],
    },
  ],
}
