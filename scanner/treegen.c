#include <stdio.h>
#include <string.h>
#include "cparse.h"
#define OPERATOR 0
#define VARIABLE 1
#define DECLARE 2

struct stack
{
    char *text;
    struct stack *prev;
};

int getNumToPop(char *op, int *type)
{
    *type = OPERATOR;
    if (!strcmp(op,"NO_OP"))
        return 0;
    else if (!strcmp(op,"-U") ||
        !strcmp(op,"+U") ||
        !strcmp(op,"++") ||
        !strcmp(op,"--") ||
        !strcmp(op,"INT") ||
        !strcmp(op,"FLOAT") ||
        !strcmp(op,"STRING") ||
        !strcmp(op,"GET_MEM") ||
        !strcmp(op,"RETURN VAL") )
            return 1;
    else if (!strcmp(op,"+") ||
        !strcmp(op,"-") ||
        !strcmp(op,";") ||
        !strcmp(op,",") ||
        !strcmp(op,"*") ||
        !strcmp(op,"/") ||
        !strcmp(op,"%") ||
        !strcmp(op,".") ||
        !strcmp(op,"=") ||
        !strcmp(op,"AND") ||
        !strcmp(op,"AND-bitwise") ||
        !strcmp(op,"OR") ||
        !strcmp(op,"==") ||
        !strcmp(op,"!=") ||
        !strcmp(op,"LT") ||
        !strcmp(op,"GT") ||
        !strcmp(op,"LE") ||
        !strcmp(op,"GE") ||
        !strcmp(op,"ACCESS") ||
        !strcmp(op,"RS") ||
        !strcmp(op,"LS") ||
        !strcmp(op,"if") ||
        !strcmp(op,"[]") ||
        !strcmp(op,"while") ||
        !strcmp(op,"FUN") ||
        !strcmp(op,"CALL"))
            return 2;
    else if (!strcmp(op,"if/else") ||
        !strcmp(op,"?"))
            return 3;
    else if (!strcmp(op,"DECLARE"))
    {
        *type = DECLARE;
        return 3;
    }
    else
    {
        *type = VARIABLE;
        return 0;
    }
}

char *pop(struct stack **s)
{
    char *out = (*s)->text;
    *s = (*s)->prev;
    return out;
}

void push(char *data, struct stack **s)
{
    struct stack *next = allocate(sizeof(struct stack));
    next->prev = *s;
    next->text = data;
    *s = next;
}

int main(int argc, char **argv)
{
    struct stack data;
    struct stack *s = &data;
    char buf[1024*8];
    int type = 0;
    printf("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>");
    printf("<program>");
    if (argc == 1)
    {
        while (fgets(buf,1024*8,stdin))
        {
            buf[strlen(buf) - 1] = '\0';
            int toPop = getNumToPop(buf,&type);
            char *element = "";

            if (type == OPERATOR)
            {
                while (toPop > 0){
                    element = concatStrings(3,element,"",pop(&s));
                    toPop--;
                }
                push(concatStrings(5,"<op name=\"",buf,"\">",element,"</op>"),&s);
            }
            else if (type == VARIABLE)
            {
                push(concatStrings(3,"<value>",buf,"</value>"),&s);
            }
            else//type == DECLARE
            {
                while (toPop > 1){
                    element = concatStrings(3,element,"",pop(&s));
                    toPop--;
                }
                char *scope = pop(&s);
                scope[strlen(scope) - 5] = '\0';
                push(concatStrings(4,scope,"<uses>",element,"</uses></op>"),&s);
            }
        }
    }
    while (s && s->text){
        printf("%s",pop(&s));
    }
    printf("</program>");
}
