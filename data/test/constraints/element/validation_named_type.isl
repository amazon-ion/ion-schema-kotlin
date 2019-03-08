schema_header::{}
type::{
  name: CustomInt,
  type: int,
}
type::{
  name: CollectionOfCustomInt,
  element: CustomInt,
}
schema_footer::{}

test_validation::{
  values: ((0 1.0) document::"0 1.0"),
  type: CollectionOfCustomInt,
  violations: [
    {
      constraint: { element: CustomInt },
      code: element_mismatch,
      children: [
        {
          index: 1,
          value: 1.0,
          violations: [
            {
              constraint: { type: CustomInt },
              code: type_mismatch,
              violations: [
                { constraint: { type: int }, code: type_mismatch },
              ],
            },
          ],
        },
      ],
    },
  ],
}

