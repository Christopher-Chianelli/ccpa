%{
    #include <stdio.h>
    #include <string.h>
    #include "cparse.h"
    #include "c_parser.tab.h"
    #define YY_DECL extern int yylex()
    extern void comment();
    extern void hashtag();
    extern void updateFileInfo(char *info);
    extern void yyerror(const char *);
%}
DOTS     "..."
DIGIT    [0-9]
LETTER   [a-zA-Z]
TYPE    "__builtin_va_list"|"const"|"volatile"|"restrict"|"_Atomic"|"unsigned"|"signed"|"char"|"short"|"long"|"int"|"float"|"double"|"void"|"_Complex"
TYPEDEF "typedef"
IDENTIFIER [a-zA-Z_][a-zA-Z_0-9]*
STAR "*"
ADDRESS "&"
EQUALS "="
SIZEOF "sizeof"
UNI "++"|"--"
OP1 "."|"->"
OP2 "!"|"&"|"~"
OP3 "/"|"%"
MINUS "-"
PLUS "+"
OP5 "<<"|">>"
OP6 "<"|"<="|">"|">="
OP7 "=="|"!="
OP9 "^"
OP10 "|"
OP11 "&&"
OP12 "||"
OP13S "?"
OP13E ":"
OP14 "+="|"-="|"*="|"/="|"%="|"<<="|">>="|"&="|"^="|"|="
WHITESPACE [ \t]+
NEWLINE \n
COMMENT "//".*
COMMENT_MULTILINE "/*"
DELIMITER ","|";"|"("|")"|"["|"]"|"{"|"}"
STRLIT \"([^\\\"]|\\.)*\"
CHARLIT \'.\'|\'\\.\'
HASHTAG (#[a-zA-Z_][a-zA-Z_0-9]*)
FILEINFO (#[ ].*)
%%
{DIGIT}+ {yylval.ival = atoi(yytext); return INT;}
{DIGIT}+u {yytext[strlen(yytext) - 1] = '\0'; yylval.ival = atoi(yytext); return INT;}
{CHARLIT} {yylval.ival = 0; return INT;}
{DIGIT}+"."{DIGIT}+ {yylval.fval = atof(yytext); return FLOAT;}
{STRLIT} {yylval.sval = strdup(yytext); return STR_LIT;}

if {return IF;}
else {return ELSE;}
for {return FOR;}
while {return WHILE;}
return {return RETURN;}
struct {ignoreTable=1;return STRUCT;}
union {ignoreTable=1;return UNION;}
enum {ignoreTable=1;return ENUM;}
extern {return EXTERN;}
inline|__inline|__inline__|__forceinline {return INLINE;}
static {return STATIC;}

; {return SEMICOLON;}
, {return COMMA;}

\( {return OPEN_BRACKET;}
\) {return CLOSE_BRACKET;}

\[ {return OPEN_SQUARE;}
\] {return CLOSE_SQUARE;}

\{ {ignoreTable=0;return OPEN_BLOCK;}
\} {return CLOSE_BLOCK;}

{MINUS} {return MINUS;}
{PLUS} {return PLUS;}
{EQUALS} {return EQUALS;}
{STAR} {return STAR;}
{SIZEOF} {return SIZEOF;}
{ADDRESS} {return ADDRESS;}
{DOTS} {return DOTS;}

{TYPEDEF} {return TYPEDEF;}
{TYPE} {yylval.sval = strdup(yytext); return TYPE;}
_Nullable {}
_Nonnull {}
{IDENTIFIER} {yylval.sval = strdup(yytext); return (isType(&yylval.sval))? TYPE : ID;}

{UNI} {yylval.sval = strdup(yytext); return UNI;}
{OP1} {yylval.sval = strdup(yytext); return OP1;}
{OP2} {yylval.sval = strdup(yytext); return OP2;}
{OP3} {yylval.sval = strdup(yytext); return OP3;}
{OP5} {yylval.sval = strdup(yytext); return OP5;}
{OP6} {yylval.sval = strdup(yytext); return OP6;}
{OP7} {yylval.sval = strdup(yytext); return OP7;}
{OP9} {yylval.sval = strdup(yytext); return OP9;}
{OP10} {yylval.sval = strdup(yytext); return OP10;}
{OP11} {yylval.sval = strdup(yytext); return OP11;}
{OP12} {yylval.sval = strdup(yytext); return OP12;}
{OP13S} {yylval.sval = strdup(yytext); return OP13S;}
{OP13E} {yylval.sval = strdup(yytext); return OP13E;}
{OP14} {yylval.sval = strdup(yytext); return OP14;}

{WHITESPACE} {}
{NEWLINE} {lineNumber++;}
{COMMENT} {}
{HASHTAG} { hashtag(); }
{FILEINFO} {updateFileInfo(yytext + 2); hashtag();}
{COMMENT_MULTILINE}                    { comment(); }

. {fprintf(stderr,"Unrecongized character: %s\n", yytext);}
%%

//Copied from http://www.quut.com/c/ANSI-C-grammar-l-2011.html#comment
void comment()
{
    int c;

    while ((c = input()) != 0)
        if (c == '*')
        {
            while ((c = input()) == '*')
                ;

            if (c == '/')
                return;

            if (c == 0)
                break;
        }
    yyerror("Unterminated comment");
}

void hashtag()
{
    int c;

    while ((c = input()) != 0)
        if (c == '\n')
        {
            return;
        }
}

void updateFileInfo(char *info)
{
    char *num = strtok(info, " ");
    lineNumber = atoi(num);
    char *name = strtok(NULL, " ");
    strcpy(filename,name);
}