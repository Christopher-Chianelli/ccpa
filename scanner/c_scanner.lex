/*
 * scanner/c_scanner.lex
 * Copyright (C) 2017 Christopher Chianelli
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 
%{
    #include <stdio.h>
    #include <string.h>
    #include "cparse.h"
    #include "c_parser.tab.h"
    #define YY_DECL extern int yylex()
    extern void comment();
    extern void hashtag();
    extern void updateFileInfo(char *info);
    extern void yyerror(const char *,...);
    char linebuf[500];

    int columnNumber = 1;

#define YY_USER_ACTION yylloc.first_line = yylloc.last_line = lineNumber; \
    yylloc.first_column = columnNumber; yylloc.last_column = columnNumber+yyleng-1; \
    columnNumber += yyleng;
%}

%option nounput
%option noyywrap

DOTS     "..."
DIGIT    [0-9]
LETTER   [a-zA-Z]
TYPE   "char"|"int"|"float"|"void"
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
do {return DO;}
while {return WHILE;}
return {return RETURN;}
break {return BREAK;}
continue {return CONTINUE;}
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
{IDENTIFIER} {yylval.sval = strdup(yytext); int enumMember = getEnumMember(yylval.sval); if (enumMember >= 0){yylval.ival = enumMember; return INT;};return (isType(&yylval.sval))? TYPE : ID;}

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
\n.*  { lineNumber++;columnNumber=0;strncpy(linebuf, yytext+1, 500); /* save the next line */
           yyless(1);      /* give back all but the \n to rescan */
      }
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
    columnNumber=0;
    char *name = strtok(NULL, " ");
    strcpy(filename,name);
}
