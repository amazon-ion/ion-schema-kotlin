// asserts that all constraints that have a type reference work when
// the referenced type is defined later in the ISL.  in such cases,
// resolution of the type reference is deferred until it is found.
//
schema_header::{
}
type::{
  name: type,
  type: type_ref,      // the 'type_ref' type is unknown at this point, so resolution is deferred
}
type::{
  name: element,
  element: type_ref,   // ditto
}
type::{
  name: fields,
  fields: {
    a: type_ref,       // ditto
  },
}
type::{
  name: ordered_elements,
  ordered_elements: [type_ref],  // ditto
}
type::{
  name: all_of,
  all_of: [type_ref],  // ditto
}
type::{
  name: any_of,
  any_of: [type_ref],  // ditto
}
type::{
  name: one_of,
  one_of: [type_ref],  // ditto
}
type::{
  name: not,
  not: type_ref,       // ditto
}
type::{
  name: type_ref,      // aha!  here it is!
  codepoint_length: 3,
}
schema_footer::{
}

valid::{
              type: [ abc ],
           element: [ [abc] ],
            fields: [ {a: abc} ],
  ordered_elements: [ [abc] ],
            all_of: [ abc ],
            any_of: [ abc ],
            one_of: [ abc ],
               not: [ ab, abcd ],
}
invalid::{
              type: [ ab, abcd ],
           element: [ [ab], [abcd] ],
            fields: [ {a: ab}, {a: abcd} ],
  ordered_elements: [ [ab], [abcd] ],
            all_of: [ ab, abcd ],
            any_of: [ ab, abcd ],
            one_of: [ ab, abcd ],
               not: [ abc ],
}

