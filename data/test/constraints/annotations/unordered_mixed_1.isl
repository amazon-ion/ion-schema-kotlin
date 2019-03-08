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

