int foo(int a, int b)
{
    int c = 0;
    return a + b;
}

int fun()
{
    int j = 0;
    for (int i = 0; i < 10; i++)
        j = j + i;
    return j;
}

int main()
{
    int c = foo(1,2);
    return fun();
}
