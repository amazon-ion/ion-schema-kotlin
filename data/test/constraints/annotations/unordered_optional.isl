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

