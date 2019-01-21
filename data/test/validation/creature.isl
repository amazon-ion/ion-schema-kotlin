schema_header::{}
type::{
  name: Cat,
  type: struct,
  fields: {
    type: { valid_values: ["cat"] },
    class: { valid_values: [mammal] },
    appendage_count: { valid_values: [4] },
  },
}
type::{
  name: Dog,
  type: struct,
  fields: {
    type: { valid_values: ["dog"] },
    class: { valid_values: [mammal] },
    appendage_count: { valid_values: [4] },
  },
}
type::{
  name: Dolphin,
  type: struct,
  fields: {
    type: { valid_values: ["dolphin"] },
    class: { valid_values: [mammal] },
    appendage_count: { valid_values: [2] },
  },
}
type::{
  name: Lizard,
  type: struct,
  fields: {
    type: { valid_values: ["lizard"] },
    class: { valid_values: [reptile] },
    appendage_count: { valid_values: [4] },
  },
}
type::{
  name: Octopus,
  type: struct,
  fields: {
    type: { valid_values: ["octopus"] },
    class: { valid_values: [cephalopod] },
    tentacle_count: { valid_values: [8], occurs: required },
  },
}
type::{
  name: Snake,
  type: struct,
  fields: {
    type: { valid_values: ["snake"] },
    class: { valid_values: [reptile] },
  },
}
type::{
  name: Creature,
  one_of: [
    Cat, Dog, Dolphin, Lizard, Octopus, Snake,
    type::{ fields: { appendage_count: { valid_values: [5] } } },
    { type: int, valid_values: range::[0, 100] },
    { type: int },
    int,
  ],
}
schema_footer::{}

test_validation::{
  value: { appendage_count: 4, class: mammal },
  type: Creature,
  violations: [
    {
      constraint: {
        one_of: [
          Cat, Dog, Dolphin, Lizard, Octopus, Snake,
          type::{ fields: { appendage_count: { valid_values: [5] } } },
          { type: int, valid_values: range::[0, 100] },
          { type: int },
          int,
        ],
      },
      code: more_than_one_type_matched,
      violations: [
        {
          constraint: { type: Dolphin },
          code: type_mismatch,
          violations: [
            {
              constraint: {
                fields: {
                  type: { valid_values: ["dolphin"] },
                  class: { valid_values: [mammal] },
                  appendage_count: { valid_values: [2] },
                },
              },
              code: fields_mismatch,
              children: [
                {
                  path: "appendage_count",
                  value: 4,
                  violations: [ { constraint: { valid_values: [2] }, code: invalid_value } ],
                },
              ],
            },
          ],
        },
        {
          constraint: { type: Lizard },
          code: type_mismatch,
          violations: [
            {
              constraint: {
                fields: {
                  type: { valid_values: ["lizard"] },
                  class: { valid_values: [reptile] },
                  appendage_count: { valid_values: [4] },
                },
              },
              code: fields_mismatch,
              children: [
                {
                  path: "class",
                  value: mammal,
                  violations: [ { constraint: { valid_values: [reptile] }, code: invalid_value } ],
                },
              ],
            },
          ],
        },
        {
          constraint: { type: Octopus },
          code: type_mismatch,
          violations: [
            {
              constraint: {
                fields: {
                  type: { valid_values: ["octopus"] },
                  class: { valid_values: [cephalopod] },
                  tentacle_count: { valid_values: [8], occurs: required },
                },
              },
              code: fields_mismatch,
              children: [
                {
                  path: "class",
                  value: mammal,
                  violations: [ { constraint: { valid_values: [cephalopod] }, code: invalid_value } ],
                },
                {
                  path: "tentacle_count",
                  violations: [ { constraint: { occurs: required }, code: occurs_mismatch } ],
                },
              ],
            },
          ],
        },
        {
          constraint: { type: Snake },
          code: type_mismatch,
          violations: [
            {
              constraint: {
                fields: {
                  type: { valid_values: ["snake"] },
                  class: { valid_values: [reptile] },
                },
              },
              code: fields_mismatch,
              children: [
                {
                  path: "class",
                  value: mammal,
                  violations: [ { constraint: { valid_values: [reptile] }, code: invalid_value } ],
                },
              ],
            },
          ],
        },
        {
          constraint: type::{ fields: { appendage_count: { valid_values: [5] } } },
          code: type_mismatch,
          violations: [
            {
              constraint: { fields: { appendage_count: { valid_values: [5] } } },
              code: fields_mismatch,
              children: [
                {
                  path: "appendage_count",
                  value: 4,
                  violations: [
                    { constraint: { valid_values: [5] }, code: invalid_value },
                  ],
                },
              ],
            },
          ],
        },
        {
          constraint: { type: int, valid_values: range::[0, 100] },
          code: type_mismatch,
          violations: [
            { constraint: { type: int }, code: type_mismatch },
            { constraint: { valid_values: range::[0, 100] }, code: invalid_value },
          ],
        },
        {
          constraint: { type: int },
          code: type_mismatch,
        },
        {
          constraint: { type: int },
          code: type_mismatch,
        },
        { constraint: { type: Cat }, code: type_matched },
        { constraint: { type: Dog }, code: type_matched },
      ],
    },
  ],
}

