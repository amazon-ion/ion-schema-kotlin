type::{
  annotations: required::ordered::[a, b, c, d],
}
valid::[
  a::b::c::d::5,
  open_content::a::open_content::b::open_content::c::open_content::d::open_content::5,
]
invalid::[
  b::c::d::5,
  a::c::d::5,
  a::b::d::5,
  a::b::c::5,
]

