%{
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include "cparse.h"
// stuff from flex that bison needs to know about:

#define ANSI_COLOR_RED     "\x1b[31m"
#define ANSI_COLOR_GREEN   "\x1b[32m"
#define ANSI_COLOR_YELLOW  "\x1b[33m"
#define ANSI_COLOR_BLUE    "\x1b[34m"
#define ANSI_COLOR_MAGENTA "\x1b[35m"
#define ANSI_COLOR_CYAN    "\x1b[36m"
#define ANSI_COLOR_RESET   "\x1b[0m"

extern int yylex();
extern int yyparse();
extern FILE *yyin;
extern char *yytext;
extern char linebuf[500];

int error = 0;
%}

%locations

//Token type
%union {
	int ival;
	float fval;
	char *sval;
    char vval;
	struct expr expression;
	struct strings *strings;
}

//Tokens
%token <ival> INT
%token <fval> FLOAT
%token <sval> STR_LIT

%token <vval> STRUCT
%token <vval> UNION
%token <vval> ENUM
%token <vval> EXTERN

%nonassoc <vval> OPEN_BRACKET
%nonassoc <vval> CLOSE_BRACKET

%token <vval> IF
%nonassoc <vval> ELSE

%token <vval> WHILE
%token <vval> FOR

%token <vval> RETURN
%token <vval> BREAK
%token <vval> CONTINUE
%token <vval> DO

%token <vval> TYPEDEF
%token <vval> DOTS
%token <vval> INLINE
%token <vval> STATIC
%token <sval> TYPE
%token <sval> ID

%left <sval> UNI//++,--
%left <vval> MINUS//-
%left <vval> PLUS//+
%left <vval> ADDRESS//&
%left <vval> STAR//*
%right <vval> SIZEOF

%left <sval> OP1//.,->
%right <sval> OP2//!,~
%left <sval> OP3//,%
%left <sval> OP5//<<,>>
%left <sval> OP6//<=,<,>,>=
%left <sval> OP7//==,!=
%left <sval> OP9//^
%left <sval> OP10//|
%left <sval> OP11//&&
%left <sval> OP12//||
%right <sval> OP13S//?
%right <sval> OP13E//:
%right <sval> OP14//+=,-=...

%right <vval> EQUALS

%token <vval> SEMICOLON
%left <vval> COMMA

%token <vval> OPEN_BLOCK
%token <vval> CLOSE_BLOCK

%token <vval> OPEN_SQUARE
%token <vval> CLOSE_SQUARE

%type <expression> global function code codeBlock callList arglist variableList variableDeclaration externStatement dataStatement ifStatement returnStatement forLoop whileLoop doWhileLoop expression typeDef assignment ternary disjunct conjunct orAble xorAble andAble equalable comparable shift sum factor prefix postfix operand functionDef breakStatement continueStatement
%type <sval> variableName variable type typecast unamedDef dataDef endScope
%type <ival> multistar
%type <strings> dataList
%%
// Grammar
program:
	global {$1.rep = concatStrings(2,$1.rep,destroyLocalScope());preorderTranversal($1);}
	;

startScope:
    /* empty */ {createLocalScope();}
	;

endScope:
    /* empty */ {$$ = destroyLocalScope();}
	;

global:
    /* empty */ {$$=createEmptyExpr();}
    | global function {$$=appendExprs($1,$2);}
	| global functionDef endScope SEMICOLON {$$=$1;declareVariable($2.type,$2.rep);}
	| global dataStatement {$$=$1;}
	| global STRUCT ID SEMICOLON {$$=$1;}
	| global UNION ID SEMICOLON {$$=$1;}
	| global ENUM ID SEMICOLON {$$=$1;}
	| global variableList SEMICOLON {$$=appendExprs($1,$2);}
	| global typeDef {$$=appendExprs($1,$2);}
	| global externStatement {$$=appendExprs($1,$2);}
    ;

externStatement:
    EXTERN functionDef endScope SEMICOLON {$$=createEmptyExpr();declareVariable($2.type,$2.rep);}
	| EXTERN variableList SEMICOLON {$$=$2;}
	| EXTERN dataStatement {$$=$2;}
	| EXTERN STRUCT ID SEMICOLON {$$=createEmptyExpr();}
	| EXTERN UNION ID SEMICOLON {$$=createEmptyExpr();}
	| EXTERN ENUM ID SEMICOLON {$$=createEmptyExpr();}
	;

functionQuantifiers:
    INLINE {}
	| STATIC INLINE {}
	;

function:
    functionDef OPEN_BLOCK codeBlock CLOSE_BLOCK endScope {$$=createExpr(concatStrings(2,"\nFUN\n",$5),$3,$1,NO_EXPR);declareVariable($1.type,$1.rep);}
    ;

functionDef:
    type ID OPEN_BRACKET startScope arglist CLOSE_BRACKET {$$=createTextExpr(functionType($1,0,$5),$2);declareVariable($$.type,$$.rep);}
	| type OPEN_BRACKET multistar ID OPEN_BRACKET arglist CLOSE_BRACKET CLOSE_BRACKET startScope OPEN_BRACKET arglist CLOSE_BRACKET {$$=createTextExpr(functionType(functionType(extractType($1),$3,$6),0,$11),$4);declareVariable($$.type,$$.rep);}
	| functionQuantifiers functionDef {$$=$2;}
	;

arglist:
	/* empty */ {$$ = createTypeExpr("");}
	| DOTS {$$ = createTypeExpr("...");}
	| type OPEN_BRACKET multistar variable CLOSE_BRACKET OPEN_BRACKET arglist CLOSE_BRACKET {$$ = createTypeExpr(functionType(extractType($1),$3,$7));declareVariable(functionType($1,$3,$7),$4);}
	| type OPEN_BRACKET multistar CLOSE_BRACKET OPEN_BRACKET arglist CLOSE_BRACKET {$$ = createTypeExpr(functionType(extractType($1),$3,$6));}
	| type {$$ = createTypeExpr(extractType($1));}
	| type variable {$$ = createTypeExpr(extractType($1));declareVariable($1,$2);}
	| type OPEN_SQUARE CLOSE_SQUARE {$$ = createTypeExpr(addStars(extractType($1),1));}
	| type OPEN_SQUARE expression CLOSE_SQUARE {$$ = createTypeExpr(addStars(extractType($1),1));}
	| arglist COMMA arglist {$$=appendExprs($1,$3);$$.type = concatStrings(3,$1.type,",",$3.type);}
	;

variableName:
    ID {$$ = $1;}
	| variableName OPEN_SQUARE CLOSE_SQUARE {$$ = $1;}
	| variableName OPEN_SQUARE type CLOSE_SQUARE {$$ = $1;}
	| variableName OPEN_SQUARE expression CLOSE_SQUARE {$$ = $1;}
	| variableName OP13E INT {$$ = $1;}
	| OP13E INT {$$ = "";}
	;

variable:
	variableName {$$ = $1;}
	| OP9 {$$ = "";}
	| OP9 variableName {$$ = $2;}
	;

variableList:
      type variable {declareVariable($1,$2);$$ = createTypeExpr($1);}
	| type OPEN_BRACKET multistar variable CLOSE_BRACKET OPEN_BRACKET arglist CLOSE_BRACKET {declareVariable(functionType($1,$3,$7),$4);$$ = createTypeExpr($1);}
	| type variable EQUALS ternary {declareVariable($1,$2);$$=createExpr("=",getVariable($2),$4,NO_EXPR);}
    | variableList COMMA multistar variable {declareVariable(addStars(getBaseType($1.type),$3),$4);$$ = $1;}
	| variableList COMMA multistar variable EQUALS ternary {declareVariable(addStars(getBaseType($1.type),$3),$4);$$=appendExprs($1,createExpr("=",getVariable($4),$6,NO_EXPR));}
	| variableList COMMA type variable {declareVariable($3,$4);$$ = $1;}
	| variableList COMMA type variable EQUALS ternary {declareVariable($3,$4);$$=appendExprs($1,createExpr("=",getVariable($4),$6,NO_EXPR));}
    ;

code:
    startScope OPEN_BLOCK codeBlock CLOSE_BLOCK endScope {$$=$3;$$.rep = concatStrings(2,$$.rep,$5);}
    | variableList SEMICOLON {$$=$1;}
    | expression SEMICOLON {$$=$1;}
    | ifStatement {$$=$1;}
    | returnStatement {$$=$1;}
	| breakStatement {$$=$1;}
	| continueStatement {$$=$1;}
    | whileLoop {$$=$1;}
	| doWhileLoop {$$=$1;}
    | forLoop {$$=$1;}
	| typeDef {$$=$1;}
	| SEMICOLON {$$=createEmptyExpr();}
	| error SEMICOLON {}
	| error CLOSE_BLOCK {}
	;

codeBlock:
    /* empty */ {$$=createEmptyExpr();}
    | codeBlock startScope OPEN_BLOCK codeBlock CLOSE_BLOCK endScope {$$=appendExprs($1,$4);$$.rep = concatStrings(2,$$.rep,$6);}
    | codeBlock variableList SEMICOLON {$$=appendExprs($1,$2);}
    | codeBlock expression SEMICOLON {$$=appendExprs($1,$2);}
    | codeBlock ifStatement {$$=appendExprs($1,$2);}
    | codeBlock returnStatement {$$=appendExprs($1,$2);}
	| codeBlock breakStatement {$$=appendExprs($1,$2);}
	| codeBlock continueStatement {$$=appendExprs($1,$2);}
    | codeBlock whileLoop {$$=appendExprs($1,$2);}
	| codeBlock doWhileLoop {$$=appendExprs($1,$2);}
    | codeBlock forLoop {$$=appendExprs($1,$2);}
	| codeBlock typeDef SEMICOLON {$$=appendExprs($1,$2);}
	| codeBlock SEMICOLON {$$=appendExprs($1,createEmptyExpr());}
	| codeBlock error SEMICOLON {}
	| codeBlock error CLOSE_BLOCK {}
	;

variableDeclaration:
    variableList {$$=$1;}
	| expression {$$=$1;}
    ;

typeDef:
    TYPEDEF type variable SEMICOLON {$$=createEmptyExpr();addType($2,$3);}
	| TYPEDEF type SEMICOLON {$$=createEmptyExpr();}
	| TYPEDEF dataDef variable SEMICOLON {$$=createEmptyExpr();addType($2,$3);}
	| TYPEDEF type OPEN_BRACKET multistar variable CLOSE_BRACKET OPEN_BRACKET arglist CLOSE_BRACKET SEMICOLON {$$=createEmptyExpr();addType(functionType($2,$4,$8),$5);}
	;

type:
    TYPE {$$=$1;}
	| STRUCT ID {$$=appendStr("struct ",$2);}
	| UNION ID {$$=appendStr("union ",$2);}
	| ENUM ID {$$="int";}
	| unamedDef {$$=appendStr("struct ", replaceNewLine($1)); }
	| type TYPE {$$=appendStr($1,$2);}
	| type STRUCT ID  {$$=appendStr("struct ",$3);}
	| type UNION ID {$$=appendStr("union ",$3);}
	| type ENUM ID  {$$="int";}
	| type STAR  {$$=addStars($1,1);}
	| type unamedDef {$$=appendStr("struct ", replaceNewLine($2));}
	;

typecast:
    OPEN_BRACKET type CLOSE_BRACKET {$$=$2;}
	;

multistar:
    /* empty */ {$$=0;}
	| multistar STAR {$$=$1 + 1;}
	;

dataStatement:
    dataDef dataList SEMICOLON {$$=createTextExpr("",$1);while($2 && $2->string){declareVariable(intIfEnum(concatStrings(3,convertStringToLowerCase(replaceNewLine(strchr($1,'\n')))," ",replaceNewLine($1))),$2->string);$2 = $2->next;}}
	;

dataDef:
     STRUCT ID OPEN_BLOCK startScope members CLOSE_BLOCK endScope {$$=concatStrings(3,$2,"\nSTRUCT\n",$7);declareStruct($2,$7);addToDefine($$);}
    | UNION ID OPEN_BLOCK startScope members CLOSE_BLOCK endScope {$$=concatStrings(3,$2,"\nUNION\n",$7);declareStruct($2,$7);addToDefine($$);}
	| ENUM ID OPEN_BLOCK startScope enumList CLOSE_BLOCK endScope {$$=concatStrings(3,$2,"\nENUM\n",$7);declareStruct($2,$7);addToDefine($$);}
	;

unamedDef:
    STRUCT OPEN_BLOCK startScope members CLOSE_BLOCK endScope {char *id = getFreshId(); $$=concatStrings(3,id,"\nSTRUCT\n",$6);declareStruct(id,$6);addToDefine($$);}
    | UNION OPEN_BLOCK startScope members CLOSE_BLOCK endScope {char *id = getFreshId(); $$=concatStrings(3,id,"\nUNION\n",$6);declareStruct(id,$6);addToDefine($$);}
    | ENUM OPEN_BLOCK startScope enumList CLOSE_BLOCK endScope {char *id = getFreshId(); $$=concatStrings(3,id,"\nENUM\n",$6);declareStruct(id,$6);addToDefine($$);}
	;


enumList:
    ID {declareEnumMember($1,-1);}
	| ID EQUALS INT {declareEnumMember($1,$3);}
	| enumList COMMA enumList {}
	;

dataList:
    /* empty */ {$$=emptyStringStack();}
    | variable {$$=emptyStringStack();addToStringStack($$,$1);}
	| dataList COMMA variable {$$=$1;addToStringStack($$,$3);}
	;

members:
    variableList SEMICOLON {}
	| dataStatement {}
	| members variableList SEMICOLON {}
	| members dataStatement {}
	;

expression:
    assignment {$$=$1;}
	| expression COMMA assignment {$$=createExpr(",",$1,$3,NO_EXPR);}
	;

assignment:
    ternary {$$=$1;}
    | assignment OP14 ternary {$$=createExpr($2,$1,$3,NO_EXPR);}
	| assignment EQUALS ternary {$$=createExpr("=",$1,$3,NO_EXPR);}
	;

ternary:
    disjunct {$$=$1;}
	| disjunct OP13S assignment OP13E assignment {$$=createExpr("?",$1,$3,$5);}
	;

disjunct:
    conjunct {$$=$1;}
	| disjunct OP12 conjunct {$$=createExpr($2,$1,$3,NO_EXPR);}
    ;

conjunct:
	orAble {}
	| conjunct OP11 orAble {$$=createExpr($2,$1,$3,NO_EXPR);}
	;

orAble:
    xorAble {}
	| orAble OP10 xorAble {$$=createExpr($2,$1,$3,NO_EXPR);}
	;

xorAble:
    andAble {}
	| xorAble OP9 andAble {$$=createExpr($2,$1,$3,NO_EXPR);}
	;

andAble:
    equalable {}
	| andAble ADDRESS equalable {$$=createExpr("&",$1,$3,NO_EXPR);}
	;

equalable:
	comparable {}
	| equalable OP7 comparable {$$=createExpr($2,$1,$3,NO_EXPR);}
	;

comparable:
    shift {}
	| comparable OP6 shift {$$=createExpr($2,$1,$3,NO_EXPR);}
	;

shift:
    sum {}
	| shift OP5 sum {$$=createExpr($2,$1,$3,NO_EXPR);}
	;

sum:
    factor {}
	| sum PLUS factor {$$=createExpr("+",$1,$3,NO_EXPR);}
	| sum MINUS factor {$$=createExpr("-",$1,$3,NO_EXPR);}
	;

factor:
    prefix {}
	| factor OP3 prefix {$$=createExpr($2,$1,$3,NO_EXPR);}
	| factor STAR prefix {$$=createExpr("*",$1,$3,NO_EXPR);}
	;

prefix:
    postfix {}
	| MINUS prefix {$$=createExpr("-U",$2,NO_EXPR,NO_EXPR);}
	| PLUS prefix {$$=createExpr("+U",$2,NO_EXPR,NO_EXPR);}
	| OP2 prefix {$$=createExpr($1,$2,NO_EXPR,NO_EXPR);}
	| STAR prefix {$$=createExpr("GET_MEM",$2,NO_EXPR,NO_EXPR);}
	| ADDRESS prefix {$$=createExpr("ADDRESS",$2,NO_EXPR,NO_EXPR);}
	| UNI prefix {$$=createExpr(concatStrings(2,"PRE",$1),$2,NO_EXPR,NO_EXPR);}
	| typecast prefix {$$=$2;$$.type=$1;}
	| SIZEOF typecast {$$=createExpr("SIZEOF",createExpr("PRE++",createTypeExpr($2),NO_EXPR,NO_EXPR),NO_EXPR,NO_EXPR);}
	| SIZEOF prefix {$$=createExpr("SIZEOF",$2,NO_EXPR,NO_EXPR);}

postfix:
    operand {}
	| postfix OPEN_SQUARE expression CLOSE_SQUARE {
		                                           struct expr sizeofExpr = createExpr("SIZEOF",createExpr("GET_MEM",$1,NO_EXPR,NO_EXPR),NO_EXPR,NO_EXPR);
	                                               struct expr productExpr = createExpr("*",$3,sizeofExpr,NO_EXPR);
												   struct expr sumExpr = createExpr("+",$1,productExpr,NO_EXPR);
												   $$=createExpr("GET_MEM",sumExpr,NO_EXPR,NO_EXPR);
											       }
	| postfix OPEN_BRACKET callList CLOSE_BRACKET {$$=createFunctionCall($1,$3);}
	| postfix OP1 ID {$$=createExpr(concatStrings(2,"GET_",$2),$1,getStructMember($1.type,$3),NO_EXPR);}
	| postfix UNI {$$=createExpr(concatStrings(2,"POST",$2),$1,NO_EXPR,NO_EXPR);}
	;

operand:
      ID {$$=getVariable($1);}
    | INT {$$=createIntExpr($1);}
    | FLOAT {$$=createFloatExpr($1);}
    | STR_LIT {$$=createStringExpr($1);}
	| OPEN_BRACKET expression CLOSE_BRACKET {$$=$2;}
    ;

callList:
    /* empty */ {$$=createEmptyExpr();}
    | assignment {$$=$1;$$.type = extractType($$.type);}
	| type {$$=createTypeExpr($1);}
	| callList COMMA assignment {$$=appendExprs($1,$3);$$.type = concatStrings(3,$1.type,",",extractType($3.type));}
	| callList COMMA type {$$=appendExprs($1,createTypeExpr($3));$$.type = concatStrings(3,$1.type,",",$3);}
    ;

ifStatement:
    IF OPEN_BRACKET expression CLOSE_BRACKET code ELSE code {$$=createExpr("if/else",$3,$5,$7);}
	| IF OPEN_BRACKET expression CLOSE_BRACKET code {$$=createExpr("if",$3,$5,NO_EXPR);}
    ;

returnStatement:
    RETURN SEMICOLON {$$=createExpr("RETURN",NO_EXPR,NO_EXPR,NO_EXPR);}
    | RETURN expression SEMICOLON {$$=createExpr("RETURN VAL",$2,NO_EXPR,NO_EXPR);}
    ;

breakStatement:
	BREAK SEMICOLON {$$=createExpr("BREAK",NO_EXPR,NO_EXPR,NO_EXPR);}
	;

continueStatement:
	CONTINUE SEMICOLON {$$=createExpr("CONTINUE",NO_EXPR,NO_EXPR,NO_EXPR);}
	;

whileLoop:
    WHILE OPEN_BRACKET expression CLOSE_BRACKET code {$$=createExpr("while",$3,$5,NO_EXPR);}
    ;

doWhileLoop:
	DO code WHILE OPEN_BRACKET expression CLOSE_BRACKET SEMICOLON {$$=createExpr("doWhile",$5,$2,NO_EXPR);}
	;

forLoop:
    FOR OPEN_BRACKET startScope variableDeclaration SEMICOLON expression SEMICOLON expression CLOSE_BRACKET code endScope {$$=createExpr("for",$6,$10,$8);$$=appendExprs($4,$$);$$.rep = concatStrings(2,$$.rep,$11);}
    | FOR OPEN_BRACKET SEMICOLON expression SEMICOLON expression CLOSE_BRACKET code {$$=createExpr("for",$4,$8,$6);}
    | FOR OPEN_BRACKET startScope variableDeclaration SEMICOLON expression SEMICOLON CLOSE_BRACKET code endScope {$$=createExpr("for",$6,$9,createEmptyExpr());$$=appendExprs($4,$$);$$.rep = concatStrings(2,$$.rep,$10);}
    | FOR OPEN_BRACKET SEMICOLON expression SEMICOLON CLOSE_BRACKET code {$$=createExpr("for",$4,$7,createEmptyExpr());}

	| FOR OPEN_BRACKET startScope variableDeclaration SEMICOLON  SEMICOLON expression CLOSE_BRACKET code endScope {$$=createExpr("for",createIntExpr(1),$9,$7);$$=appendExprs($4,$$);$$.rep = concatStrings(2,$$.rep,$10);}
    | FOR OPEN_BRACKET SEMICOLON SEMICOLON expression CLOSE_BRACKET code {$$=createExpr("for",createIntExpr(1),$7,$5);}
    | FOR OPEN_BRACKET startScope variableDeclaration SEMICOLON SEMICOLON CLOSE_BRACKET code endScope {$$=createExpr("for",createIntExpr(1),$8,createEmptyExpr());$$=appendExprs($4,$$);$$.rep = concatStrings(2,$$.rep,$9);}
    | FOR OPEN_BRACKET SEMICOLON SEMICOLON CLOSE_BRACKET code {$$=createExpr("for",createIntExpr(1),$6,createEmptyExpr());}
    ;
%%

int main(int argc, char** argv) {
    ++argv, --argc;  /* skip over program name */
    if ( argc > 0 )
            yyin = fopen( argv[0], "r" );
    else
            yyin = stdin;

	my_typedefs.type = NULL;
	my_typedefs.next = NULL;

	existingStrings.string = "";
	existingStrings.next = NULL;

	NO_EXPR.type = "";
	NO_EXPR.rep = "";

	toDefine.string = NULL;
	toDefine.next = NULL;

	variables = malloc(sizeof(struct variableStack));
	variables->prev = NULL;
	variables->vars = malloc(sizeof(struct variableList));
	variables->vars->type = NULL;
	variables->vars->name = NULL;
	variables->vars->next = NULL;

	structs.name = NULL;
	structs.members = NULL;
	structs.next = NULL;

	declareVariable("void(char*,...)","printf");
	declareVariable("int()","readInt");
	declareVariable("float()","readFloat");

	// parse through the input until there is no more:
	do {
		yyparse();
	} while (!feof(yyin));
	return error;
}

void yyerror(const char *s, ...)
{
    va_list ap;
	va_start(ap, s);
    if(yylloc.first_line)
    {
		fprintf(stderr, ANSI_COLOR_YELLOW "%s " ANSI_COLOR_RESET "%d.%d-%d.%d "
	            ANSI_COLOR_RED "error: " ANSI_COLOR_RESET, filename, yylloc.first_line, yylloc.first_column,
	                    yylloc.last_line, yylloc.last_column);
    }
    vfprintf(stderr, s, ap);
    fprintf(stderr, "\n");
	fprintf(stderr, ANSI_COLOR_CYAN "Error happened here: %d. ", yylloc.first_line);
	for (int i = 0; i < yylloc.first_column; i++)
	    fputc(linebuf[i],stderr);
	fprintf(stderr, ANSI_COLOR_YELLOW);
	for (int i = yylloc.first_column; i <= yylloc.last_column; i++)
		fputc(linebuf[i],stderr);
	fprintf(stderr, ANSI_COLOR_CYAN);
	for (int i = yylloc.last_column + 1; linebuf[i]; i++)
		fputc(linebuf[i],stderr);
	fprintf(stderr,ANSI_COLOR_RESET "\n");
    error = 1;
}

void yywarn(const char *s, ...)
{
	va_list ap;
	va_start(ap, s);
    if(yylloc.first_line)
    {
	    fprintf(stderr, ANSI_COLOR_YELLOW "%s " ANSI_COLOR_RESET "%d.%d-%d.%d "
	            ANSI_COLOR_MAGENTA "warning: " ANSI_COLOR_RESET, filename, yylloc.first_line, yylloc.first_column,
	                    yylloc.last_line, yylloc.last_column);
    }
    vfprintf(stderr, s, ap);
    fprintf(stderr, "\n");
	fprintf(stderr, ANSI_COLOR_CYAN "Happened here: %d. %s\n" ANSI_COLOR_RESET, yylloc.first_line, linebuf);
}
