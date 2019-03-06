// invalid specification
invalid_type::{ timestamp_precision: null }
invalid_type::{ timestamp_precision: null.symbol }
invalid_type::{ timestamp_precision: null.list }
invalid_type::{ timestamp_precision: 5 }
invalid_type::{ timestamp_precision: min }
invalid_type::{ timestamp_precision: max }
invalid_type::{ timestamp_precision: [year, second] }

// wrong # of elements in list
invalid_type::{ timestamp_precision: range::[year] }
invalid_type::{ timestamp_precision: range::[year, second, nanosecond] }

// lower > upper
invalid_type::{ timestamp_precision: range::[max, min] }
invalid_type::{ timestamp_precision: range::[year, min] }
invalid_type::{ timestamp_precision: range::[max, nanosecond] }
invalid_type::{ timestamp_precision: range::[month, year] }
invalid_type::{ timestamp_precision: range::[day, month] }
invalid_type::{ timestamp_precision: range::[minute, day] }
invalid_type::{ timestamp_precision: range::[second, minute] }
invalid_type::{ timestamp_precision: range::[millisecond, second] }
invalid_type::{ timestamp_precision: range::[microsecond, millisecond] }
invalid_type::{ timestamp_precision: range::[nanosecond, microsecond] }

// empty range
invalid_type::{ timestamp_precision: range::[exclusive::day, exclusive::day] }

