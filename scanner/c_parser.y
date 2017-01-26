%{
#include <stdio.h>
#include <stdlib.h>
// stuff from flex that bison needs to know about:
extern int yylex();
extern int yyparse();
extern FILE *yyin;

void yyerror(const char *s);
%}

//Token type
%union {
	int ival;
	float fval;
	char *sval;
    char vval;
}

//Tokens
%token <ival> INT
%token <fval> FLOAT
%token <sval> STR_LIT

%token <vval> STRUCT
%token <vval> EXTERN

%nonassoc <vval> OPEN_BRACKET
%nonassoc <vval> CLOSE_BRACKET

%token <vval> IF
%nonassoc <vval> ELSE

%token <vval> WHILE
%token <vval> FOR

%token <vval> RETURN

%token <sval> TYPE
%token <sval> ID

%left <sval> UNI //++, --
%left <sval> MINUS //-
%left <sval> OP1 //., ->
%left <vval> STAR
%right <sval> OP2 //!, &
%left <sval> OP3 //* / %
%left <sval> OP4 //+ -
%left <sval> OP5 //<= >= < >
%left <sval> OP6 //== !=
%left <sval> OP7 //&&
%left <sval> OP8 //||
%right <sval> OP9 //+=, -=,...
%right <vval> EQUALS

%token <vval> SEMICOLON
%left <vval> COMMA

%token <vval> OPEN_BLOCK
%token <vval> CLOSE_BLOCK

%token <vval> OPEN_SQUARE
%token <vval> CLOSE_SQUARE
%%
// Grammar
program:
	global {}
	;

global:
    /* empty */ {}
    | function global {printf("Parser: Function Declared\n");}
	| structDef global {printf("Parser: Struct Declared\n");}
    | EXTERN variableDeclaration SEMICOLON global {printf("Parser: Global variable declared\n");}
    ;

function:
    type ID OPEN_BRACKET arglist CLOSE_BRACKET OPEN_BLOCK codeBlock CLOSE_BLOCK {printf("Parser: Function\n");}
    ;

arglist:
    /* empty */ {printf("Parser: Empty arg list\n");}
    | type variable {printf("Parser: arg\n");}
    | type variable COMMA arglist {printf("Parser: arg list\n");}
    ;

code:
    OPEN_BLOCK codeBlock CLOSE_BLOCK {printf("Parser: Code Block\n");}
    | variableDeclaration SEMICOLON {printf("Parser: Declare Statement\n");}
    | expression SEMICOLON {printf("Parser: Expression Statement\n");}
    | ifStatement {printf("Parser: IF Statement\n");}
    | returnStatement {printf("Parser: Return Statement\n");}
    | whileLoop {printf("Parser: While Statement\n");}
    | forLoop {printf("Parser: For Statement\n");}

codeBlock:
    /* empty */ {}
    | OPEN_BLOCK codeBlock CLOSE_BLOCK codeBlock {printf("Parser: Code Statement\n");}
    | variableDeclaration SEMICOLON codeBlock {printf("Parser: Declare Statement\n");}
    | expression SEMICOLON codeBlock {printf("Parser: Expression Statement\n");}
    | ifStatement codeBlock {printf("Parser: IF Statement\n");}
    | returnStatement codeBlock {printf("Parser: Return Statement\n");}
    | whileLoop codeBlock {printf("Parser: While Statement\n");}
    | forLoop codeBlock {printf("Parser: For Statement\n");}

variableDeclaration:
    type expression {printf("Parser: Var Declaration\n");}
    ;

type:
    TYPE {printf("Parser: Type\n");}
	| STRUCT ID {printf("Parser: Struct Type\n");}
	;

structDef:
    STRUCT ID OPEN_BLOCK members CLOSE_BLOCK SEMICOLON{printf("Parser: Struct\n");}
	;

members:
    variableDeclaration SEMICOLON {printf("Parser: Struct Var\n");}
	| variableDeclaration SEMICOLON members {printf("Parser: Struct Var\n");}
	;

variable:
    ID {printf("Parser: Var\n");}
	| STAR variable {printf("Parser: Pointer Variable\n");}

expression:
    assignment {printf("Parser: Expression\n");}
	| expression COMMA assignment {printf("Parser: List Statement\n");}
	;

assignment:
    disjunct {}
    | assignment OP9 disjunct {printf("Parser: Assignment\n");}
	| assignment EQUALS disjunct {printf("Parser: Assignment\n");}
	;

disjunct:
    conjunct {}
	| disjunct OP8 conjunct {printf("Parser: OR\n");}
    ;

conjunct:
	equalable {}
	| conjunct OP7 equalable {printf("Parser: AND\n");}
	;

equalable:
	comparable {}
	| equalable OP6 comparable {printf("Parser: Equality\n");}
	;

comparable:
    sum {}
	| comparable OP5 sum {printf("Parser: Comparision\n");}
	;

sum:
    factor {}
	| sum OP4 factor {printf("Parser: Sum\n");}
	| sum MINUS factor {printf("Parser: Difference\n");}
	;

factor:
    term {}
	| factor OP3 term {printf("Parser: Product\n");}
	| factor STAR term {printf("Parser: Product\n");}
	;

term:
    operand {}
	| MINUS operand {printf("Parser: Negation\n");}
	| OP2 term {printf("Parser: Uni Op Left\n");}
	| STAR term {printf("Parser: Uni Op Left\n");}
	| UNI operand {printf("Parser: Uni Op Left\n");}

operand:
    ID {printf("Parser: Variable\n");}
    | INT {printf("Parser: Int\n");}
    | FLOAT {printf("Parser: Float\n");}
    | STR_LIT {printf("Parser: String\n");}
    | ID OPEN_BRACKET callList CLOSE_BRACKET {printf("Parser: Function Call\n");}
	| operand OP1 ID {printf("Parser: Get\n");}
	| operand UNI {printf("Parser: Uni Op Right\n");}
	| operand OPEN_SQUARE expression CLOSE_SQUARE {printf("Parser: Array Access\n");}
	| OPEN_BRACKET expression CLOSE_BRACKET {printf("Parser: Change Order of Ops\n");}
    ;

callList:
    /* empty */ {printf("Parser: Empty Call List\n");}
    | expression {printf("Parser: Call Item\n");}
    ;

ifStatement:
    IF OPEN_BRACKET expression CLOSE_BRACKET code ELSE code {printf("Parser: IF WITH ELSE\n");}
	| IF OPEN_BRACKET expression CLOSE_BRACKET code {printf("Parser: IF\n");}
    ;

returnStatement:
    RETURN SEMICOLON {printf("Parser: RETURN\n");}
    | RETURN expression SEMICOLON {printf("Parser: RETURN VALUE\n");}
    ;

whileLoop:
    WHILE OPEN_BRACKET expression CLOSE_BRACKET code {printf("Parser: WHILE LOOP\n");}
    ;

forLoop:
    FOR OPEN_BRACKET variableDeclaration SEMICOLON expression SEMICOLON expression CLOSE_BRACKET code {printf("Parser: FOR LOOP\n");}
    | FOR OPEN_BRACKET SEMICOLON expression SEMICOLON expression CLOSE_BRACKET code {printf("Parser: FOR LOOP\n");}
    | FOR OPEN_BRACKET variableDeclaration SEMICOLON expression SEMICOLON CLOSE_BRACKET code {printf("Parser: FOR LOOP\n");}
    | FOR OPEN_BRACKET SEMICOLON expression SEMICOLON CLOSE_BRACKET code {printf("Parser: FOR LOOP\n");}
    ;
%%

int main(int argc, char** argv) {
    ++argv, --argc;  /* skip over program name */
    if ( argc > 0 )
            yyin = fopen( argv[0], "r" );
    else
            yyin = stdin;
	// parse through the input until there is no more:
	do {
		yyparse();
	} while (!feof(yyin));
}

void yyerror(const char *s) {
	fprintf(stderr, "Error: %s\n",s);
	exit(1);
}
