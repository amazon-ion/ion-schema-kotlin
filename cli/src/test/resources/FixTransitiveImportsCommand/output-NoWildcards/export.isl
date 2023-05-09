$ion_schema_1_0

// Schema 'export.isl'
// 
// The purpose of this schema is to decouple consumers of the schema from the
// implementation details (ie. specific locations) of each type that it provides,
// and to indicate to consumers, which types they SHOULD use. Consumers of this
// type CAN bypass this schema and import other types directly, but they SHOULD NOT
// unless directed to do so by the owner(s)/author(s) of this schema.
//
// The type
//     type::{name:foobar,type:{id:"bar.isl",type:foo}}
// is analogous to
//   [Javascript]: export { foo as foobar } from 'bar.isl'
//         [Rust]: pub use bar::foo as foobar;


type::{name:negative_float,type:{id:"floats.isl",type:negative_float}}
type::{name:negative_int,type:{id:"ints.isl",type:negative_int}}
type::{name:positive_float,type:{id:"floats.isl",type:positive_float}}
type::{name:positive_int,type:{id:"ints.isl",type:positive_int}}
