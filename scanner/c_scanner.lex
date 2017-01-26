%{
    #include <stdio.h>
    #include "c_parser.tab.h"
    #define YY_DECL extern int yylex()
%}

DIGIT    [0-9]
LETTER   [a-zA-Z]
TYPE    "int"|"float"|"void"
IDENTIFIER [a-zA-Z_][a-zA-Z_0-9]*
STAR "*"
EQUALS "="
UNI "++"|"--"
OP1 "."|"->"
OP2 "!"|"&"
OP3 "/"|"%"
MINUS "-"
OP4 "+"
OP5 "<"|"<="|">"|">="
OP6 "=="|"!="
OP7 "&&"
OP8 "||"
OP9 "+="|"-="|"*="|"/="|"%="
WHITESPACE [ \t\n]+
DELIMITER ","|";"|"("|")"|"["|"]"|"{"|"}"
STRLIT \"([^\\\"]|\\.)*\"
%%
{DIGIT}+ {yylval.ival = atoi(yytext); return INT;}
{DIGIT}+"."{DIGIT}+ {yylval.fval = atof(yytext); return FLOAT;}
{STRLIT} {yylval.sval = strdup(yytext); return STR_LIT;}

if {return IF;}
else {return ELSE;}
for {return FOR;}
while {return WHILE;}
return {return RETURN;}
struct {return STRUCT;}
extern {return EXTERN;}

; {return SEMICOLON;}
, {return COMMA;}

\( {return OPEN_BRACKET;}
\) {return CLOSE_BRACKET;}

\[ {return OPEN_SQUARE;}
\] {return CLOSE_SQUARE;}

\{ {return OPEN_BLOCK;}
\} {return CLOSE_BLOCK;}

{MINUS} {return MINUS;}
{EQUALS} {return EQUALS;}
{STAR} {return STAR;}

{TYPE} {yylval.sval = strdup(yytext); return TYPE;}
{IDENTIFIER} {yylval.sval = strdup(yytext); return ID;}

{UNI} {yylval.sval = strdup(yytext); return UNI;}
{OP1} {yylval.sval = strdup(yytext); return OP1;}
{OP2} {yylval.sval = strdup(yytext); return OP2;}
{OP3} {yylval.sval = strdup(yytext); return OP3;}
{OP4} {yylval.sval = strdup(yytext); return OP4;}
{OP5} {yylval.sval = strdup(yytext); return OP5;}
{OP6} {yylval.sval = strdup(yytext); return OP6;}
{OP7} {yylval.sval = strdup(yytext); return OP7;}
{OP8} {yylval.sval = strdup(yytext); return OP8;}
{OP9} {yylval.sval = strdup(yytext); return OP9;}

{WHITESPACE} {}
. {fprintf(stderr,"Unrecongized character: %s\n", yytext);}
%%
