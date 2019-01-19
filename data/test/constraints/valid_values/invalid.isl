// annotations are not allowed
invalid_type::{ valid_values: [ hello::5 ] }

// bad ranges
invalid_type::{ valid_values: [ min::1, max::0 ] }
invalid_type::{ valid_values: [ min::0.00000000001, max::0 ] }
