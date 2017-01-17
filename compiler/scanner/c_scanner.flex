Program         ::= (Whitespace | Token)*

Token           ::= ID | Integer | Operator | Delimiter
ID              ::= Letter (Letter | Digit)*
Integer         ::= Digit+
Operator        ::= "+" | "-" | "*" | "/" | "%"
Delimiter       ::= ";" | "=" | "[" | "]"

Whitespace      ::= <space> | <tab> | <newline>
Letter          ::= "a" | ... | "z" | "A" | ... | "Z"
Digit           ::= "0" | ... | "9"
