#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include "cparse.h"

int lineNumber;
int ignoreTable;
struct typedefs my_typedefs;
struct strings existingStrings;
struct variableStack *variables;
struct expr NO_EXPR;
char filename[256];

void addType(char *type, char *name)
{
	struct typedefs *temp = &my_typedefs;
	if (temp->type == NULL)
	{
		temp->type = name;
		temp->realType = type;
		temp->next = NULL;
		return;
	}
	while (temp->next != NULL)
	{
		temp = temp->next;
	}
	temp->next = allocate(sizeof(struct typedefs));
	temp->next->type = name;
	temp->next->realType = type;
	temp->next->next = NULL;
}

int isType(char **name)
{
    struct typedefs *temp = &my_typedefs;
	if (*name == NULL)
	    return 0;

    if (ignoreTable)
    {
        ignoreTable=0;
        return 0;
    }
    while (temp && temp->type)
    {
        if (!strcmp(temp->type,*name))
        {
            *name = temp->realType;
            return 1;
        }
        temp = temp->next;
    }
    return 0;
}

void declareVariable(char *type, char *name)
{
	struct variableList *temp = variables->vars;
	if (findVariable(variables, name) != NULL)
	{
		fprintf(stderr, "%s line %d: Error: Variable \"%s\" already defined\n",filename,lineNumber,name);
		return;
	}
	if (temp->type == NULL)
	{
		temp->type = type;
		temp->name = name;
		temp->next = NULL;
		return;
	}
	while (temp->next != NULL)
	{
		temp = temp->next;
	}
	temp->next = allocate(sizeof(struct variableList));
	temp->next->name = name;
	temp->next->type = type;
	temp->next->next = NULL;
}
char *findVariable(struct variableStack *stack, char *name)
{
	if (stack == NULL || name == NULL)
	    return NULL;

	struct variableList *temp = stack->vars;
    while (temp && temp->type)
    {
        if (!strcmp(temp->name,name))
        {
            return temp->type;
        }
        temp = temp->next;
    }
    return findVariable(stack->prev,name);
}

char *variableType(char *name)
{
	return findVariable(variables,name);
}

void createLocalScope()
{
	struct variableStack *newScope = malloc(sizeof(struct variableStack));
	newScope->prev = variables;
	newScope->vars = malloc(sizeof(struct variableList));
	newScope->vars->type = NULL;
	newScope->vars->name = NULL;
	newScope->vars->next = NULL;
	variables = newScope;
}

char *destroyLocalScope()
{
	struct variableList *temp = variables->vars;
	char *out = "";

	while (temp && temp->next)
	{
		out = concatStrings(5,temp->name,"\n",temp->type,"\nDECLARE\n",out);
		temp = temp->next;
	}
	if (temp && temp->name)
	    out = (!strcmp(out,""))?
		concatStrings(5,temp->name,"\n",temp->type,"\nDECLARE",out) :
		concatStrings(5,temp->name,"\n",temp->type,"\nDECLARE\n",out);
	freeVariableList(variables->vars);
	struct variableStack *toFree = variables;
	variables = variables->prev;
	free(toFree);
	return out;
}

struct expr createExpr(char *op, struct expr arg1, struct expr arg2, struct expr arg3)
{
    struct expr out;
    out.type = getType(op,arg1.type,arg2.type,arg3.type);

	if (out.type == NULL)
	{
		fprintf(stderr, "%s line %d: Error: Incompatiable types for %s: %s %s %s\n",filename,lineNumber,op, arg1.type, arg2.type, arg3.type);
		out.type = "";
	}
	//Change ops with angle brackets to XML friendly format
	if (!strcmp(op,"<"))
	    op = "LT";
	else if (!strcmp(op,">"))
	    op = "GT";
	else if (!strcmp(op,"<="))
		op = "LE";
	else if (!strcmp(op,">="))
		op = "GE";
	else if (!strcmp(op,"->"))
		op = "ACCESS";
	else if (!strcmp(op,">>"))
		op = "RS";
	else if (!strcmp(op,"<<"))
		op = "LS";
	else if (!strcmp(op,"&"))
		op = "AND-bitwise";
	else if (!strcmp(op,"&="))
		op = "SET-AND-bitwise";
	else if (!strcmp(op,"&&"))
		op = "AND";
	else if (!strcmp(op,"||"))
		op = "OR";
    out.rep = concatStrings(5,arg1.rep,arg2.rep,arg3.rep,op,"\n");
    return out;
}

struct expr createEmptyExpr()
{
    struct expr out;
    out.type = "";
    out.rep = "NO_OP\n";
    return out;
}

struct expr getVariable(char *var)
{
	struct expr out;
    out.type = variableType(var);
	out.rep = concatStrings(2,var,"\n");

	if (out.type == NULL)
	{
		fprintf(stderr, "%s line %d: Undefined Variable: %s\n",filename, lineNumber, var);
		out.type = "";
	}
    return out;
}

struct expr createIntExpr(int i)
{
	struct expr out;
    out.type = "int";
	out.rep = concatStrings(2,findOrCreateString("%d",i),"\nINT\n");

    return out;
}

struct expr createFloatExpr(float f)
{
	struct expr out;
    out.type = "float";
	out.rep = concatStrings(2,findOrCreateString("%f",f),"\nFLOAT\n");

    return out;
}

struct expr createStringExpr(char *s)
{
	struct expr out;
    out.type = "char*";
	out.rep = concatStrings(2,s,"\nSTRING\n");

    return out;
}

struct expr createVariableExpr(char *type, char *name)
{
	struct expr out;
    out.type = type;
	out.rep = concatStrings(2,name,"\n");

    return out;
}

struct expr createFunctionCall(struct expr fun, struct expr args)
{
	struct expr out;
    out.type = functionTypeAfterCall(fun.type);
	out.rep = concatStrings(3,args.rep,fun.rep,"CALL\n");

	if (!strcmp(out.type,"")){
		fprintf(stderr,"%s line %d: Error: ",filename,lineNumber);
		printWithoutNewline(fun.rep);
	    fprintf(stderr," is not a function\n");
		return out;
	}

	char *funArgs = getFunctionArgsType(fun.type);
	int canCallOut = canCall(funArgs,args.type);
	if (canCallOut == 0){
		fprintf(stderr,"%s line %d: Error: ",filename,lineNumber);
		printWithoutNewline(fun.rep);
	    fprintf(stderr, " takes %s as arguments; recieved %s\n",funArgs,args.type);
	}
	else if (canCallOut == -1){
		fprintf(stderr,"%s line %d: Warning: ",filename,lineNumber);
		printWithoutNewline(fun.rep);
		fprintf(stderr, " takes %s as arguments; recieved %s\n",funArgs,args.type);
	}

    return out;
}

struct expr createTypeExpr(char *type)
{
    struct expr out = createExpr("",NO_EXPR,NO_EXPR,NO_EXPR);
	out.type = type;
	return out;
}

struct expr createTextExpr(char *type, char *text)
{
    struct expr out;
	out.type = type;
	out.rep = text;
	return out;
}

struct expr appendExprs(struct expr a, struct expr b)
{
    return createExpr(";",a,b,NO_EXPR);
}

char *functionType(char *returnType, int numOfPointers, struct expr args)
{
    char *out = concatStrings(4,returnType,"(",args.type,")");
    while (numOfPointers > 0)
    {
        out = concatStrings(2,out,"*");
        numOfPointers--;
    }

    return out;
}

char *getFunctionArgsType(char *fun)
{
	char *start = fun + strlen(fun);
	while (start != fun && *start != '(')
	    start--;
	start++;
	int length = 0;
	while (start[length] != '\0' && start[length] != ')')
	    length++;
	length++;
	char *out = malloc(sizeof(char)*length);
	strncpy(out,start,length);
	out[length - 1] = '\0';
	char *buf = findOrCreateString("%s",out);
	free(out);

	return buf;
}

int canCall(char *funArgsType, char *args)
{
	if (funArgsType == NULL || args == NULL)
	    return 0;
	char *funContext, *argContext;
	char *funType, *argType;
	char *funBuf = malloc(sizeof(char)*(strlen(funArgsType) + 1));
	char *argsBuf = malloc(sizeof(char)*(strlen(args) + 1));
	int warn = 1;

	strcpy(funBuf,funArgsType);
	strcpy(argsBuf,args);

	for (funType = strtok_r(funBuf,",",&funContext),
	     argType = strtok_r(argsBuf,",",&argContext);
		 funType;
		 funType = strtok_r(NULL,",",&funContext),
	     argType = strtok_r(NULL,",",&argContext))
	{
		if (!strcmp(funType,"...")){
			free(funBuf);
			free(argsBuf);
		    return warn;
		}
		if (argType == NULL)
		    return 0;
		if (strcmp(argType,funType) && (isPointer(funType) && isPointer(argType))){
			warn = -1;
		}

		if (argType == NULL || (strcmp(argType,funType) && (!isPointer(funType) || !isPointer(argType)))){
			free(funBuf);
			free(argsBuf);
		    return 0;
		}
	}
	free(funBuf);
	free(argsBuf);
	return warn;
}

char *appendStr(char *a, char *b)
{
    return concatStrings(2,a,b);
}

char *addStars(char *a, int numOfStars)
{
    char *out = a;

    while (numOfStars > 0)
    {
        out = concatStrings(2,out,"*");
        numOfStars--;
    }

    return out;
}

char *getBaseType(char *type)
{
    char *out = allocate(sizeof(char)*strlen(type));
	strcpy(out,type);
    int pos = strlen(out) - 1;

    while (out[pos] == '*')
    {
        out[pos] = '\0';
        pos--;
    }
	char *buf = findOrCreateString("%s",out);
	free(out);
    return buf;
}

char *getType(char *op, char *arg1, char *arg2, char *arg3)
{
	if (op == NULL)
	    return NULL;
    if (!strcmp(op,"+") || !strcmp(op,"-") || !strcmp(op,"*") || !strcmp(op,"/") || !strcmp(op,"%"))
        return betterType(arg1,arg2);
	else if (!strcmp(op,"=") || !strcmp(op,"+=") ||!strcmp(op,"-=") ||!strcmp(op,"/=") ||!strcmp(op,"*=") ||!strcmp(op,"&=") ||!strcmp(op,"|=") || !strcmp(op,"^=") || !strcmp(op,"[]"))
	    return arg1;
	else if (!strcmp(op,";") || !strcmp(op,","))
	    return arg2;
	else if (!strcmp(op,"||") || !strcmp(op,"&&") || !strcmp(op,"==") || !strcmp(op,"!=") || !strcmp(op,">=") || !strcmp(op,"<=") || !strcmp(op,"<") || !strcmp(op,">"))
	    return "int";
    return "";
}

char *betterType(char *a, char *b)
{
	if (a == NULL || b == NULL)
	    return NULL;
    char *type1 = extractType(a);
    char *type2 = extractType(b);
	int aNum = !(isCompositeType(type1) || isPointer(type1));
	int bNum = !(isCompositeType(type2) || isPointer(type2));

	if (isPointer(type1) && !strcmp(type2,"int"))
	    return a;
	else if (isPointer(type2) && !strcmp(type1,"int"))
	    return b;
    else if (!strcmp(type1,type2))
    {
        return a;
    }
    else if (!strcmp(type2,"char") && aNum)
    {
        return a;
    }
    else if (!strcmp(type1,"char") && bNum)
    {
        return b;
    }
    else if (!strcmp(type2,"short") && aNum)
    {
        return a;
    }
    else if (!strcmp(type1,"short") && bNum)
    {
        return b;
    }
    else if (!strcmp(type2,"int") && aNum)
    {
        return a;
    }
    else if (!strcmp(type1,"int") && bNum)
    {
        return b;
    }
    else if (!strcmp(type2,"long") && aNum)
    {
        return a;
    }
    else if (!strcmp(type1,"long") && bNum)
    {
        return b;
    }
    else if (!strcmp(type2,"long long") && aNum)
    {
        return a;
    }
    else if (!strcmp(type1,"long long") && bNum)
    {
        return b;
    }
    else if (!strcmp(type1,"float") && aNum)
    {
        return b;
    }
    else if (!strcmp(type1,"float") && bNum)
    {
        return b;
    }
    else
    {
        return NULL;
    }
}

char *extractType(char *type)
{
	char *old = type;

    while (   *type != '\0' && (!prefix(type,"char")
           && !prefix(type,"short")
           && !prefix(type,"int")
           && !prefix(type,"long")
           && !prefix(type,"long long")
           && !prefix(type,"float")
           && !prefix(type,"double")
           && !prefix(type,"struct")
           && !prefix(type,"union")
           && !prefix(type,"enum")
           && !prefix(type,"void"))
       )
           type++;

	int stars = 0;

    if (prefix(type,"char")){
		type += 4;
		while (*type == '*'){
		    stars++;
			type++;
		}
	    return addStars("char",stars);
	}
	else if (prefix(type,"short")){
		type += 5;
		while (*type == '*'){
		    stars++;
			type++;
		}
	    return addStars("short",stars);
	}
	else if (prefix(type,"int")){
		type += 3;
		while (*type == '*'){
		    stars++;
			type++;
		}
	    return addStars("int",stars);
	}
	else if (prefix(type,"long long")){
		type += 9;
		while (*type == '*'){
		    stars++;
			type++;
		}
	    return addStars("long long",stars);
	}
	else if (prefix(type,"long")){
		type += 4;
		while (*type == '*'){
		    stars++;
			type++;
		}
	    return addStars("long",stars);
	}
	else if (prefix(type,"float")){
		type += 5;
		while (*type == '*'){
		    stars++;
			type++;
		}
	    return addStars("float",stars);
	}
	else if (prefix(type,"double")){
		type += 6;
		while (*type == '*'){
		    stars++;
			type++;
		}
	    return addStars("double",stars);
	}
	else if (prefix(type,"struct")){
		return type;
	}
	else if (prefix(type,"union")){
		return type;
	}
	else if (prefix(type,"enum")){
		return type;
	}
	else if (prefix(type,"void")){
		type += 4;
		while (*type == '*'){
		    stars++;
			type++;
		}
	    return addStars("void",stars);
	}
	return old;
}

int prefix(char *str, char *substr)
{
    return strncmp(substr, str, strlen(substr)) == 0;
}

int isCompositeType(char *type)
{
    return strstr(type, "struct") != NULL ||
           strstr(type, "union") != NULL ||
           strstr(type, "enum") != NULL;
}

int isPointer(char *type)
{
    return type[strlen(type) - 1] == '*';
}

char *functionTypeAfterCall(char *type)
{
	char *out;
	if (type == NULL)
	    return NULL;
	int pos = strlen(type) - 1;

	while (type[pos] != '(')
	    pos--;

	if (pos < 2){
	    return "";
	}

	out = allocate(sizeof(char)*pos);
	strncpy(out,type,pos - 1);
	out[pos] = '\0';
	char *buf = findOrCreateString(out);

	free(out);
	return buf;
}

void preorderTranversal(struct expr tree)
{
    printf("%s\n", tree.rep);
}

void *allocate(int s)
{
	if (s <= 0)
	{
		fprintf(stderr,"FATAL ERROR: Negative Memory Allocated\n");
	    exit(1);
	}
	void *out = malloc(s);

	if (out)
	    return out;
	else
	{
		fprintf(stderr,"FATAL ERROR: Out of memory\n");
	    exit(1);
	}

	return NULL;
}

char *concatStrings(int n_args, ...)
{
	va_list ap;
	char *out;
	struct strings *start = malloc(sizeof(struct strings));
	struct strings *temp = start;

	va_start(ap, n_args);
    for(int i = 1; i < n_args; i++) {
		temp->string = va_arg(ap, char *);
		temp->next = malloc(sizeof(struct strings));
		temp = temp->next;
    }
	temp->string = va_arg(ap, char *);
	temp->next = NULL;
    va_end(ap);

	temp = start;
	int length = 1;
	while (temp != NULL)
	{
	    length += strlen(temp->string);
		temp = temp->next;
	}

	out = malloc(sizeof(char)*length);
	temp = start;
	strcpy(out,temp->string);
	temp = temp->next;

	while (temp != NULL)
	{
		strcat(out,temp->string);
		temp = temp->next;
	}
	/*
	char *string = existingString(out);
	if (string)
	{
		free(out);
		out = string;
	}
	else
	{
		addToExistingStrings(out);
	}
	*/
	freeStrings(start);
	return out;
}

char *findOrCreateString(char *format, ...)
{
	va_list ap;
	char *buffer = malloc(128*sizeof(char));
	va_start(ap, format);
    vsnprintf(buffer,128,format,ap);
    va_end(ap);

	/*char *out = existingString(buffer);
	if (out)
	{
		free(buffer);
		return out;
	}
	else
	{
		addToExistingStrings(buffer);
		return buffer;
	}
	*/
	return buffer;
}

char *existingString(char *s)
{
	struct strings *temp = &existingStrings;
	if (s == NULL)
	    return NULL;
	while (temp != NULL && temp->string != NULL)
	{
		if (!strcmp(s,temp->string))
		    return temp->string;
		temp = temp->next;
	}
	return NULL;
}

void addToExistingStrings(char *s)
{
	struct strings *temp = &existingStrings;
	while (temp->next != NULL)
	{
		temp = temp->next;
	}
	temp->next = allocate(sizeof(struct strings));
	temp = temp->next;

	temp->string = s;
	temp->next = NULL;
}

void freeStrings(struct strings *s)
{
	if (s == NULL)
	    return;
	freeStrings(s->next);
	free(s);
}

void freeVariableList(struct variableList *vl)
{
	if (vl == NULL)
	    return;
	freeVariableList(vl->next);
	free(vl);
}

void printWithoutNewline(char *str)
{
	while (*str != '\n'){
	    fputc(*str,stderr);
		str++;
	}
}
