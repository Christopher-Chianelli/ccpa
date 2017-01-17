Program         ::= Token*
Token           ::= OperationCard |
                    NumberCard |
                    LoadCard |
                    StoreCard |
                    CombinatorialCard |
                    ActionCard

Operation       ::= "+" | "-" | "/" | "*"
OperationCard   ::= Operation <newline>

CardNumber      ::= Digit Digit Digit
Number          ::= Digit+ | "-" Digit+
NumberCard      ::= "N" CardNumber <space> Number <newline>

LoadType        ::= "L" | "Z"
LoadCard        ::= LoadType CardNumber <newline>

StoreCard       ::= "S" (CardNumber | CardNumber "'") <newline>

Direction       ::= "F" | "B"
Conditional     ::= "?" | "+"
CombinatorialCard    ::= "C" Direction Conditional <newline>

ActionCard      ::= "H" <newline> |
                    "P" <newline> |
                    AttendantCard

AttendantAction ::= "increment variable cards by" <space> Number |
                    "decrement variable cards by" <space> Number |
                    "notes location of next card on stack" |
                    "returns to location on the top of the stack"
AttendantCard   ::= "A" <space> AttendantAction
