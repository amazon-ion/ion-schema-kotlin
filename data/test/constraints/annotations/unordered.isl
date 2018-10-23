type::{
  annotations: [a, b, c, d],
}
valid::[
  5,
  a::5,
  b::5,
  c::5,
  d::5,
  a::b::c::d::5,
  d::c::b::a::5,
  open_content::a::open_content::b::open_content::c::open_content::d::open_content::5,
]

type::{
  annotations: required::[a, b, c, d],
}
valid::[
  a::b::c::d::5,
  b::a::d::c::5,
  d::c::b::a::5,
  open_content::a::open_content::b::open_content::c::open_content::d::open_content::5,
]
invalid::[
  b::c::d::5,
  a::c::d::5,
  a::b::d::5,
  a::b::c::5,
]

type::{
  annotations: [required::a, b, required::c, d],
}
valid::[
  a::c::5,
  c::a::5,
  b::a::d::c::5,
  open_content::a::open_content::c::open_content::5,
  open_content::a::open_content::b::open_content::c::open_content::d::open_content::5,
]
invalid::[
  5,
  a::5,
  c::5,
  b::c::d::5,
  a::b::d::5,
]

// same as previous, expressed differently
type::{
  annotations: required::[a, optional::b, c, optional::d],
}
valid::[
  a::c::5,
  c::a::5,
  b::a::d::c::5,
  open_content::a::open_content::c::open_content::5,
  open_content::a::open_content::b::open_content::c::open_content::d::open_content::5,
]
invalid::[
  5,
  a::5,
  c::5,
  b::c::d::5,
  a::b::d::5,
]
