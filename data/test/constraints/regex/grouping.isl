type::{
  regex: "123(ab|cd|ef)456",
}
valid::[
  "123ab456",
  "123cd456",
  "123ef456",
]
invalid::[
  "123a456",
  "123ac456",
  "123ace456",
  "123bdf456",
]
