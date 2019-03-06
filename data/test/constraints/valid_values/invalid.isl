invalid_type::{ valid_values: null }
invalid_type::{ valid_values: null.int }
invalid_type::{ valid_values: null.list }
invalid_type::{ valid_values: 5 }
invalid_type::{ valid_values: () }
invalid_type::{ valid_values: {} }

// annotations are not allowed
invalid_type::{ valid_values: [ hello::5 ] }

// bad ranges
invalid_type::{ valid_values: range::[ 1, 0 ] }
invalid_type::{ valid_values: range::[ 0.00000000001, 0 ] }
invalid_type::{ valid_values: range::[ min, max ] }

