%{
#include <stdio.h>
#include <stdlib.h>
#include "cparse.h"
// stuff from flex that bison needs to know about:
extern int yylex();
extern int yyparse();
extern FILE *yyin;
extern char *yytext;

void yyerror(const char *s);
%}

//Token type
%union {
	int ival;
	float fval;
	char *sval;
    char vval;
	struct expr expression;
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

%type <expression> global function code codeBlock callList arglist variableList variableDeclaration externStatement dataStatement ifStatement returnStatement forLoop whileLoop expression typeDef assignment ternary disjunct conjunct orAble xorAble andAble equalable comparable shift sum factor term operand functionDef
%type <sval> variableName variable type typecast unamedDef dataDef endScope
%type <ival> multistar
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
	//| global functionDef ignore endScope SEMICOLON {$$=$1;declareVariable($2.type,$2.rep);}
	| global dataStatement {$$=appendExprs($1,$2);}
	| global STRUCT ID SEMICOLON {$$=$1;}
	| global UNION ID SEMICOLON {$$=$1;}
	| global ENUM ID SEMICOLON {$$=$1;}
	| global variableList SEMICOLON {$$=appendExprs($1,$2);}
	//| global ID OPEN_BRACKET ignore CLOSE_BRACKET {$$=$1;}
	| global typeDef {$$=appendExprs($1,$2);}
	| global externStatement {$$=appendExprs($1,$2);}
    ;

externStatement:
    EXTERN functionDef endScope SEMICOLON {$$=createEmptyExpr();declareVariable($2.type,$2.rep);}
	//| EXTERN functionDef ignore endScope SEMICOLON {$$=createEmptyExpr();declareVariable($2.type,$2.rep);}
	| EXTERN variableList SEMICOLON {$$=$2;}
	| EXTERN dataStatement {$$=$2;}
	| EXTERN STRUCT ID SEMICOLON {$$=createEmptyExpr();}
	| EXTERN UNION ID SEMICOLON {$$=createEmptyExpr();}
	| EXTERN ENUM ID SEMICOLON {$$=createEmptyExpr();}
	;

/*ignore:
    ID {}
	| STR_LIT {}
	| INT {}
	| FLOAT {}
	| OPEN_BRACKET ignoreInBrackets CLOSE_BRACKET {}
	| OPEN_BRACKET TYPE CLOSE_BRACKET {}
	| ignore ignore {}
	;

ignoreInBrackets:
     {}
    | ignore {}
	| ignoreInBrackets COMMA ignore {}
	| ignoreInBrackets EQUALS ignore {}
	;*/

functionQuantifiers:
    INLINE {}
	| STATIC INLINE {}
	//| INLINE ignore  {}
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
	//| type variable ignore {declareVariable($1,$2);$$ = createTypeExpr($1);}
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
    | whileLoop {$$=$1;}
    | forLoop {$$=$1;}
	| typeDef {$$=$1;}
	| SEMICOLON {$$=createEmptyExpr();}
	;

codeBlock:
    /* empty */ {$$=createEmptyExpr();}
    | codeBlock startScope OPEN_BLOCK codeBlock CLOSE_BLOCK endScope {$$=appendExprs($1,$4);$$.rep = concatStrings(2,$$.rep,$6);}
    | codeBlock variableList SEMICOLON {$$=appendExprs($1,$2);}
    | codeBlock expression SEMICOLON {$$=appendExprs($1,$2);}
    | codeBlock ifStatement {$$=appendExprs($1,$2);}
    | codeBlock returnStatement {$$=appendExprs($1,$2);}
    | codeBlock whileLoop {$$=appendExprs($1,$2);}
    | codeBlock forLoop {$$=appendExprs($1,$2);}
	| codeBlock typeDef SEMICOLON {$$=appendExprs($1,$2);}
	| codeBlock SEMICOLON {$$=appendExprs($1,createEmptyExpr());}
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
	| UNION ID {$$=appendStr("struct ",$2);}
	| ENUM ID {$$=appendStr("struct ",$2);}
	| unamedDef {$$=$1;}
	| type TYPE {$$=appendStr($1,$2);}
	| type STRUCT ID  {$$=appendStr("struct ",$3);}
	| type UNION ID {$$=appendStr("union ",$3);}
	| type ENUM ID  {$$=appendStr("enum ",$3);}
	| type STAR  {$$=addStars($1,1);}
	| type unamedDef {$$=$2;}
	;

typecast:
    OPEN_BRACKET type CLOSE_BRACKET {$$=$2;}
	;

multistar:
    /* empty */ {$$=0;}
	| multistar STAR {$$=$1 + 1;}
	;

dataStatement:
    dataDef dataList SEMICOLON {$$=createTextExpr("",$1);}
	;

dataDef:
     STRUCT ID OPEN_BLOCK startScope members CLOSE_BLOCK endScope {$$=concatStrings(3,$2,"\nSTRUCT\n",$7);declareStruct($2,$7);}
    | UNION ID OPEN_BLOCK startScope members CLOSE_BLOCK endScope {$$=concatStrings(3,$2,"\nUNION\n",$7);declareStruct($2,$7);}
	| ENUM ID OPEN_BLOCK startScope enumList CLOSE_BLOCK endScope {$$=concatStrings(3,$2,"\nENUM\n",$7);declareStruct($2,$7);}
	;

unamedDef:
    STRUCT OPEN_BLOCK startScope members CLOSE_BLOCK endScope {$$=concatStrings(2,"UNAMED_STRUCT\n",$6);}
    | UNION OPEN_BLOCK startScope members CLOSE_BLOCK endScope {$$=concatStrings(2,"UNAMED_UNION\n",$6);}
    | ENUM OPEN_BLOCK startScope enumList CLOSE_BLOCK endScope {$$=concatStrings(2,"UNAMED_ENUM\n",$6);}
	;


enumList:
    ID {declareVariable("int",$1);}
	| ID EQUALS expression {declareVariable("int",$1);}
	//| ID ignore EQUALS expression {}
	| enumList COMMA enumList {}
	;

dataList:
    /* empty */ {}
    | variable {}
	| dataList COMMA variable {}
	;

members:
    variableList SEMICOLON {}
	| dataDef dataList SEMICOLON {}
	| variableList SEMICOLON members {}
	| dataDef dataList SEMICOLON members {}
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
    term {}
	| factor OP3 term {$$=createExpr($2,$1,$3,NO_EXPR);}
	| factor STAR term {$$=createExpr("*",$1,$3,NO_EXPR);}
	;

term:
    operand {}
	| MINUS operand {$$=createExpr("-U",$2,NO_EXPR,NO_EXPR);}
	| PLUS operand {$$=createExpr("+U",$2,NO_EXPR,NO_EXPR);}
	| OP2 term {$$=createExpr($1,$2,NO_EXPR,NO_EXPR);}
	| STAR term {$$=createExpr("GET_MEM",$2,NO_EXPR,NO_EXPR);}
	| ADDRESS operand {$$=createExpr("ADDRESS",$2,NO_EXPR,NO_EXPR);}
	| UNI operand {$$=createExpr(concatStrings(2,"PRE",$1),$2,NO_EXPR,NO_EXPR);}
	| typecast term {$$=$2;$$.type=$1;}
	| SIZEOF typecast {$$=createExpr("sizeof",createVariableExpr($2,$2),NO_EXPR,NO_EXPR);}
	| SIZEOF operand {$$=createExpr("sizeof",$2,NO_EXPR,NO_EXPR);}
	;

operand:
      ID {$$=getVariable($1);}
    | INT {$$=createIntExpr($1);}
    | FLOAT {$$=createFloatExpr($1);}
    | STR_LIT {$$=createStringExpr($1);}
    | operand OPEN_BRACKET callList CLOSE_BRACKET {$$=createFunctionCall($1,$3);}
	| ID OP1 ID {$$=createExpr(concatStrings(2,"GET_",$2),getVariable($1),getStructMember(variableType($1),$3),NO_EXPR);}
	| operand UNI {$$=createExpr(concatStrings(2,"POST",$2),$1,NO_EXPR,NO_EXPR);}
	| operand OPEN_SQUARE expression CLOSE_SQUARE {$$=createExpr("[]",$1,$3,NO_EXPR);}
	| OPEN_BRACKET expression CLOSE_BRACKET {$$=$2;}
	| operand ID {$$=createEmptyExpr();}
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

whileLoop:
    WHILE OPEN_BRACKET expression CLOSE_BRACKET code {$$=createExpr("while",$3,$5,NO_EXPR);}
    ;

forLoop:
    FOR OPEN_BRACKET startScope variableDeclaration SEMICOLON expression SEMICOLON expression CLOSE_BRACKET code endScope {$$=createExpr(concatStrings(2,";\n",$11),$4,createExpr("while",$6,createExpr(";",$10,$8,NO_EXPR),NO_EXPR),NO_EXPR);}
    | FOR OPEN_BRACKET SEMICOLON expression SEMICOLON expression CLOSE_BRACKET code {$$=createExpr("while",$4,createExpr(";",$8,$6,NO_EXPR),NO_EXPR);}
    | FOR OPEN_BRACKET startScope variableDeclaration SEMICOLON expression SEMICOLON CLOSE_BRACKET code endScope {$$=createExpr(concatStrings(2,";\n",$10),$4,createExpr("while",$6,$9,NO_EXPR),createTextExpr("",$10));}
    | FOR OPEN_BRACKET SEMICOLON expression SEMICOLON CLOSE_BRACKET code {$$=createExpr("while",$4,$7,NO_EXPR);}
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

	variables = malloc(sizeof(struct variableStack));
	variables->prev = NULL;
	variables->vars = malloc(sizeof(struct variableList));
	variables->vars->type = NULL;
	variables->vars->name = NULL;
	variables->vars->next = NULL;

	structs.name = NULL;
	structs.members = NULL;
	structs.next = NULL;

	// parse through the input until there is no more:
	do {
		yyparse();
	} while (!feof(yyin));
}

void yyerror(const char *s) {
	fprintf(stderr, "%s %d: Syntax Error: %s\n",filename, lineNumber, yytext);
	exit(1);
}
