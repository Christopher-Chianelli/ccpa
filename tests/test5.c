extern void printf(char *s,...);
extern int readInt();

void main()
{    
    printf("0 exit\n1 add\n2 subtract\n3 multiply\n4 divide\n5 mod\n6 AND\n7 OR\n");
    int input = readInt();
    while (input)
    {
        int a,b;
        a = readInt();
        b = readInt();
        
        if (input == 1)
        {
            printf("%d + %d = %d\n",a,b,a+b);
        }
        if (input == 2)
        {
            printf("%d - %d = %d\n",a,b,a-b);
        }
        if (input == 3)
        {
            printf("%d * %d = %d\n",a,b,a*b);
        }
        if (input == 4)
        {
            printf("%d / %d = %d\n",a,b,a/b);
        }
        if (input == 5)
        {
            printf("%d mod %d = %d\n",a,b,a%b);
        }
        if (input == 6)
        {
            printf("%d AND %d = %d\n",a,b,a&&b);
        }
        if (input == 7)
        {
            printf("%d OR %d = %d\n",a,b,a||b);
        }
        printf("0 exit\n1 add\n2 subtract\n3 multiply\n4 divide\n5 mod\n6 AND\n7 OR\n");
        input = readInt();
    }
}
