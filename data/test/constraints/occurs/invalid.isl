invalid_type::{ occurs: null }
invalid_type::{ occurs: null.int }
invalid_type::{ occurs: null.symbol }
invalid_type::{ occurs: null.list }
invalid_type::{ occurs: x }
invalid_type::{ occurs: -1 }
invalid_type::{ occurs: () }
invalid_type::{ occurs: {} }
invalid_type::{ occurs: [0, 1] }
invalid_type::{ occurs: range::[min, max] }
invalid_type::{ occurs: range::[1, 0] }
invalid_type::{ occurs: range::[1] }
invalid_type::{ occurs: range::[0, 1, 2] }
invalid_type::{ occurs: range::[0d0, 1] }
invalid_type::{ occurs: range::[0e0, 1] }
invalid_type::{ occurs: range::[0, 1d0] }
invalid_type::{ occurs: range::[0, 1e0] }

invalid_type::{ fields: { a: { occurs: 0 } } }
invalid_type::{ fields: { a: { occurs: range::[0, 0] } } }
invalid_type::{ fields: { a: { occurs: range::[1, 0] } } }
invalid_type::{ fields: { a: { occurs: range::[-1, 1] } } }
invalid_type::{ fields: { a: { occurs: range::[-2, -1] } } }
invalid_type::{ fields: { a: { occurs: range::[exclusive::1, exclusive::1] } } }
invalid_type::{ fields: { a: { occurs: range::[1, exclusive::2] } } }
invalid_type::{ fields: { a: { occurs: range::[exclusive::1, 2] } } }

