struct test
{
    int a;
    int b;
    struct
    {
        char *text;
        struct
        {
            int ha;
            float what;
        }e,f;
    }c;
};

void main()
{
    struct test a;
    a.a = 1;
    a.b = 2;
    a.c.text = "hello";
}
