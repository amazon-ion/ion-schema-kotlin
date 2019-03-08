type::{
  contains: [true, 1, 2.0, '3', "4", [5], (6), {a: 7} ],
}
valid::[
  [[5], '3', {a: 7}, true, 2.0, "4", (6), 1, extra_value],
  ([5]  '3'  {a: 7}  true  2.0  "4"  (6)  1  extra_value),
  document::'''[5] '3' {a: 7} true 2.0 "4" (6) 1 extra_value''',
]
invalid::[
  null,
  null.null,
  null.int,
  null.list,
  null.sexp,
  null.struct,
  [true, 1, 2.0, '3', "4", [5], (6)],
  document::'''true 1 2.0 '3' "4" [5] (6)''',
]

