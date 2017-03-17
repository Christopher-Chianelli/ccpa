struct test
{
    int a;
    struct
    {
        int d;
        int f;
    }c;
    int b;
    
};

void main()
{
    struct test e;
    struct test *other = &e;
    other->a = -5;
    other->b = 8;
    other->c.d = 9;
    other->c.f = 7;
    printf("%d\n", other->a);
    printf("%d\n", e.b);
    printf("%d\n", e.c.d);
    printf("%d\n", e.c.f);
}
