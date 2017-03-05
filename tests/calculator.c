void main()
{
    int mode;
    printf("0 exit\n1 int\n2 float\n");
    while (mode = readInt())
    {
        if (mode == 1)
        {
            printf("0 add\n1 sub\n2 mult\n3 div\n4 mod\n5 AND\n6 OR\n7 XOR\n8 BOTH\n9 AT LEAST ONE\n");
            int op = readInt();
            printf("Enter a number\n");
            int a = readInt();
            printf("Enter a number\n");
            int b = readInt();
            
            if (op == 0)
            printf("%d + %d = %d\n",a,b,a + b);
            else if (op == 1)
            printf("%d - %d = %d\n",a,b,a - b);
            else if (op == 2)
            printf("%d * %d = %d\n",a,b,a * b);
            else if (op == 3)
            printf("%d / %d = %d\n",a,b,a / b);
            else if (op == 4)
            printf("%d mod %d = %d\n",a,b,a % b);
            else if (op == 5)
            printf("%d AND %d = %d\n",a,b,a & b);
            else if (op == 6)
            printf("%d OR %d = %d\n",a,b,a | b);
            else if (op == 7)
            printf("%d XOR %d = %d\n",a,b,a ^ b);
            else if (op == 8)
            printf("BOTH %d AND %d = %d\n",a,b,a && b);
            else if (op == 9)
            printf("AT LEAST ONE OF %d OR %d = %d\n",a,b,a || b);
        }
        else if (mode == 2)
        {
            printf("0 add\n1 sub\n2 mult\n3 div\n");
            int op = readInt();
            printf("Enter a number\n");
            float a = readFloat();
            printf("Enter a number\n");
            float b = readFloat();
            
            if (op == 0)
            printf("%f + %f = %f\n",a,b,a + b);
            else if (op == 1)
            printf("%f - %f = %f\n",a,b,a - b);
            else if (op == 2)
            printf("%f * %f = %f\n",a,b,a * b);
            else if (op == 3)
            printf("%f / %f = %f\n",a,b,a / b);
        }
        printf("0 exit\n1 int\n2 float\n");
    }
}
