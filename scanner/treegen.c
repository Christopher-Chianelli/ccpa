#include <stdio.h>
#include <string.h>
#include "cparse.h"
#define OPERATOR 0
#define VARIABLE 1
#define DECLARE 2
#define DATA 3

struct stack
{
    char *text;
    struct stack *prev;
};

struct stack *s;

char *pop(struct stack **s)
{
    char *out = (*s)->text;
    *s = (*s)->prev;
    return out;
}

int getNumToPop(char *op, int *opType, char **type)
{
    *opType = OPERATOR;
    *type = strrchr(op,':');
    if (*type)
    {
        **type = '\0';
        (*type)++;
    }
    else
    {
        *type = "";
    }

    if (!strcmp(op,"NO_OP") ||
        !strcmp(op,"RETURN"))
            return 0;
    else if (!strcmp(op,"-U") ||
        !strcmp(op,"+U") ||
        !strcmp(op,"~") ||
        !strcmp(op,"PRE++") ||
        !strcmp(op,"PRE--") ||
        !strcmp(op,"POST++") ||
        !strcmp(op,"POST--") ||
        !strcmp(op,"INT") ||
        !strcmp(op,"FLOAT") ||
        !strcmp(op,"STRING") ||
        !strcmp(op,"GET_MEM") ||
        !strcmp(op,"ADDRESS") ||
        !strcmp(op,"RETURN VAL") ||
        !strcmp(op,"sizeof"))
            return 1;
    else if (!strcmp(op,"+") ||
        !strcmp(op,"-") ||
        !strcmp(op,";") ||
        !strcmp(op,",") ||
        !strcmp(op,"*") ||
        !strcmp(op,"/") ||
        !strcmp(op,"%") ||
        !strcmp(op,"^") ||
        !strcmp(op,"AND") ||
        !strcmp(op,"AND-bitwise") ||
        !strcmp(op,"OR") ||
        !strcmp(op,"OR-bitwise") ||
        !strcmp(op,"GET_MEMBER") ||
        !strcmp(op,"GET_ACCESS") ||
        !strcmp(op,"[]") ||
        !strcmp(op,"=") ||
        !strcmp(op,"+=") ||
        !strcmp(op,"-=") ||
        !strcmp(op,"*=") ||
        !strcmp(op,"/=") ||
        !strcmp(op,"%=") ||
        !strcmp(op,"^=") ||
        !strcmp(op,"AND-bitwise=") ||
        !strcmp(op,"OR-bitwise=") ||
        !strcmp(op,"LS=") ||
        !strcmp(op,"RS=") ||
        !strcmp(op,"==") ||
        !strcmp(op,"!=") ||
        !strcmp(op,"LT") ||
        !strcmp(op,"GT") ||
        !strcmp(op,"LE") ||
        !strcmp(op,"GE") ||
        !strcmp(op,"RS") ||
        !strcmp(op,"LS") ||
        !strcmp(op,"if") ||
        !strcmp(op,"while")||
        !strcmp(op,"FUN") ||
        !strcmp(op,"CALL")
        )
            return 2;
    else if (!strcmp(op,"if/else") ||
        !strcmp(op,"?"))
            return 3;
    else if (!strcmp(op,"DECLARE"))
    {
        *opType = DECLARE;
        return 3;
    }
    else if (!strcmp(op,"STRUCT"))
    {
        *opType = DATA;
        return 1;
    }
    else if (!strcmp(op,"UNAMED_STRUCT"))
    {
        *opType = DATA;
        return 0;
    }
    else
    {
        *opType = VARIABLE;
        return 0;
    }
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
    s = &data;
    data.prev = NULL;
    data.text = NULL;
    char buf[1024*8];
    int opType = 0;
    char *type;
    //printf("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>");
    printf("<program>");
    if (argc == 1)
    {
        while (fgets(buf,1024*8,stdin))
        {
            buf[strlen(buf) - 1] = '\0';
            int toPop = getNumToPop(buf,&opType,&type);
            char *element = "";

            if (opType == OPERATOR)
            {
                while (toPop > 0){
                    element = concatStrings(3,element,"",pop(&s));
                    toPop--;
                }
                push(concatStrings(7,"<op name=\"",buf,"\" type=\"",type,"\">",element,"</op>"),&s);
            }
            else if (opType == VARIABLE)
            {
                push(concatStrings(3,"<value>",buf,"</value>"),&s);
            }
            else if (opType == DECLARE)
            {
                while (toPop > 1){
                    element = concatStrings(3,element,"",pop(&s));
                    toPop--;
                }
                char *scope = pop(&s);
                char *end = scope + strlen(scope);
                while (*end != '<')
                    end--;
                char *endTag = findOrCreateString("%s",end);
                *end = '\0';

                push(concatStrings(5,scope,"<uses>",element,"</uses>",endTag),&s);
            }
            else if (opType == DATA)
            {
                while (toPop > 0){
                    element = concatStrings(3,element,"",pop(&s));
                    toPop--;
                }
                push(concatStrings(5,"<data kind=\"",buf,"\">",element,"</data>"),&s);
            }
        }
    }
    while (s && s->text){
        printf("%s",pop(&s));
    }
    printf("</program>");
}
