enum date
{
    DAY,MONTH = 1 << 2,YEAR
} me;

enum another
{
    FUCK, HELL
};

struct hello
{
    int a;
    int b;
};

void main()
{
    me = YEAR;
    struct hello test;
    test.a = 2;
    test.b = 3;
    enum another day = FUCK;
    printf("%d\n",me);
    printf("%d\n",MONTH);
    printf("%d\n",YEAR);
    printf("%d\n",FUCK);
    printf("%d\n",HELL);
    printf("%d\n",test.a);
    printf("%d\n",test.b);
    printf("%d\n",day);
}
