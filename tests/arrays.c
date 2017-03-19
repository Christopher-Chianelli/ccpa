struct test
{
    int a;
    int b;
};

void main()
{
    int a0 = 0;
    int a1 = 1;
    int a2 = 2;
    int a3 = 3;
    int a4 = 4;
    int a5 = 5;
    int *a = &a0;
    
    struct test b0;
    struct test b1;
    struct test *b = &b0;
    
    b[0].a = -1;
    b[0].b = -2;
    b[1].a = -3;
    b[1].b = -4;
    
    for (int i = 0; i < 6; i++)
    {
        a[i] = 6 - i;
    }
    
    for (int i = 0; i < 6; i++)
    {
        printf("%d\n",a[i]);
    }
    
    printf("%d\n", b);
    printf("%d\n", b[0].a);
    printf("%d\n", b[0].b);
    printf("%d\n", b[1].a);
    printf("%d\n", b[1].b);
    printf("%d\n", sizeof(struct test));
    b0.a = 0;b1.a = 0;//So optimizer does not remove the variables
}
