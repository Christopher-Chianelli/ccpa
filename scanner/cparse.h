struct typedefs
{
    char *type;
    char *realType;
    struct typedefs *next;
};

struct variable
{
    char *type;
    char *name;
};

struct variableList
{
    char *type;
    char *name;
    struct variableList *next;
};

struct structList
{
    char *name;
    char *members;
    struct structList *next;
};

struct enumList
{
    char *name;
    int value;
    struct enumList *next;
};

struct variableStack
{
    struct variableStack *prev;
    struct variableList *vars;
};

struct arraySizeStack
{
    int size;
    struct arraySizeStack *next;
};

union value{int ival; float fval; char *sval;struct variable vval;};

struct expr
{
    char *type;
    char *rep;
};

struct list
{
    char *type;
    char *name;
    struct expr *val;
    struct list *next;
};

struct strings
{
    char *string;
    struct strings *next;
};

extern struct typedefs my_typedefs;
extern struct strings existingStrings;
extern struct strings toDefine;
extern struct variableStack *variables;
extern struct structList structs;
extern struct expr NO_EXPR;
extern struct enumList enumMembers;
extern int ignoreTable;
extern int lineNumber;
extern char filename[256];
extern struct arraySizeStack arraySizes;

extern void addType(char *name, char *type);
extern int isType(char **type);
extern void declareVariable(char *type, char *name);
extern void declareEnumMember(char *name, int val);
extern int getEnumMember(char *member);
extern void declareStruct(char *name, char *members);
extern void createLocalScope();
extern char *destroyLocalScope();
extern char *findVariable(struct variableStack *stack, char *name);
extern struct expr getStructMember(char *name, char *member);
extern char *variableType(char *name);

extern struct expr createExpr(char *op, struct expr arg1, struct expr arg2, struct expr arg3);
extern struct expr createVariableExpr(char *type, char *name);
extern struct expr appendExprs(struct expr a, struct expr b);
extern struct expr createEmptyExpr();
extern struct expr createTypeExpr(char *type);
extern struct expr createTextExpr(char *type,char *text);
extern struct expr getVariable(char *var);
extern struct expr createIntExpr(int i);
extern struct expr createFloatExpr(float f);
extern struct expr createStringExpr(char *s);
extern struct expr createFunctionCall(struct expr fun, struct expr args);

extern char *appendStr(char *a, char *b);

extern char *addStars(char *a, int numOfStars);
extern char *removeStars(const char *a, int numOfStars);
extern char *getBaseType(char *type);
extern char *functionType(char *returnType, int numOfPointers, struct expr args);
extern char *getType(char *op, char *arg1, char *arg2, char *arg3);
extern char *betterType(char *a, char *b);
extern char *extractType(char *type);
extern int prefix(char *str, char *substr);
extern int isCompositeType(char *type);
extern int isPointer(char *type);
extern int canCall(char *funArgsType, char *args);
extern char *functionTypeAfterCall(char *type);
extern char *getFunctionArgsType(char *fun);

extern void preorderTranversal(struct expr tree);
extern void *allocate(int s);
extern char *concatStrings(int n_args,...);
extern void addToExistingStrings(char *s);
extern char *existingString(char *s);
extern char *findOrCreateString(char *format, ...);

extern void freeStrings(struct strings *s);
extern void freeVariableList(struct variableList *vl);

extern void printWithoutNewline(char *str);
extern char *getFreshId();
extern char *replaceNewLine(char *str);
extern void addToDefine(char *item);
extern struct strings *emptyStringStack();
extern void addToStringStack(struct strings *stack, char *item);

extern void yyerror(const char *s,...);
extern void yywarn(const char *s,...);
extern char *convertStringToLowerCase(char *s);
extern char *intIfEnum(char *type);
extern void pushArraySize(int size);
