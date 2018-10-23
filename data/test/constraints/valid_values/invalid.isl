// null are not allowed
invalid_type::{ valid_values: [ null ] }
invalid_type::{ valid_values: [ null.int ] }
invalid_type::{ valid_values: [ hello::null ] }

// annotations are not allowed
invalid_type::{ valid_values: [ hello::5 ] }

// bad ranges
invalid_type::{ valid_values: [ min::1, max::0 ] }
invalid_type::{ valid_values: [ min::0.00000000001, max::0 ] }
