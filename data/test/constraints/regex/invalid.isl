invalid_type::{ regex: null }
invalid_type::{ regex: null.string }
invalid_type::{ regex: 5 }
invalid_type::{ regex: [] }
invalid_type::{ regex: () }
invalid_type::{ regex: {} }

// backreferences are not allowed
invalid_type::{ regex: "\\1" }

// POSIX character classes are not allowed
invalid_type::{ regex: "\\p{Lower}" }

// invalid escaped character
invalid_type::{ regex: "\\z" }

// invalid character classes/ranges
invalid_type::{ regex: "[a-cd]" }
invalid_type::{ regex: "[^a-cd]" }
invalid_type::{ regex: "[a-zA-Z]" }
invalid_type::{ regex: "[a-d[m-p]]" }
invalid_type::{ regex: "[a-z&&[def]]" }
invalid_type::{ regex: "[\\\\p{L}]" }

// reluctant quantifiers are not allowed
invalid_type::{ regex: "abc??" }
invalid_type::{ regex: "abc*?" }
invalid_type::{ regex: "abc+?" }
invalid_type::{ regex: "abc{1}?" }
invalid_type::{ regex: "abc{1,}?" }
invalid_type::{ regex: "abc{1,2}?" }

// possessive quantifiers are not allowed
invalid_type::{ regex: "abc?+" }
invalid_type::{ regex: "abc*+" }
invalid_type::{ regex: "abc++" }
invalid_type::{ regex: "abc{1}+" }
invalid_type::{ regex: "abc{1,}+" }
invalid_type::{ regex: "abc{1,2}+" }

// special constructs with prefix "(?" are not allowed
invalid_type::{ regex: "(?" }

