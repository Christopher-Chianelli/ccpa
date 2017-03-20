struct test
{
    int a;
    int b;
};

void main()
{
    struct test a0[2];
    struct test a1[2];
    
    struct test *a[2];
    a[0] = a0;
    a[1] = a1;
    
    for (int i = 0; i < 2; i++)
    {
        for (int j = 0; j < 2; j++){
            a[i][j].a = 2 - i + j;
            a[i][j].b = 2 + i - j;
        }
    }
    
    for (int i = 0; i < 2; i++)
    {
        for (int j = 0; j < 2; j++){
            printf("a %d\n", a[i][j].a);
            printf("b %d\n", a[i][j].b);
        }
    }
    
    printf("\n%d\n", a0[0].a);
    printf("\n%d\n", a0[0].b);
    printf("\n%d\n", a0[1].a);
    printf("\n%d\n", a0[1].b);
    
    printf("\n%d\n", a1[0].a);
    printf("\n%d\n", a1[0].b);
    printf("\n%d\n", a1[1].a);
    printf("\n%d\n", a1[1].b);
    
}
