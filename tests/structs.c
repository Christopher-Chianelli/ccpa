struct test
{
    int a;
    int b;
    struct
    {
        int more;
    }c;
    int d[2];
    int f;
};

void main()
{
    struct test a,b;
    a.a = -1;
    a.b = 2;
    a.c.more = 3;
    
    b.a = 1;
    b.b = -2;
    b.c.more = -3;
    
    printf("%d\n",&a.a);
    printf("%d\n",&a.b);
    printf("%d\n",&a.c);
    printf("%d\n",&a.d[0]);
    printf("%d\n",&a.d[1]);
    printf("%d\n",&a.f);
    printf("-------\n");
    
    printf("%d\n", a.a);
    printf("%d\n", a.b);
    printf("%d\n", a.c.more);
    
    printf("%d\n", b.a);
    printf("%d\n", b.b);
    printf("%d\n", b.c.more);
}
