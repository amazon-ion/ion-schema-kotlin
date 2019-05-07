type::{
  ordered_elements: [
    { type: int, occurs: optional },
    int,
    symbol,
  ],
}

valid::[
  [1, a],
  [1, 2, a],
]

invalid::[
  [1],
  [a],
  [1, 2, 3, a],
]

