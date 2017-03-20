struct test
{
    int a;
    int b;
};

void main()
{
    struct test a[2][2];
    
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
}
