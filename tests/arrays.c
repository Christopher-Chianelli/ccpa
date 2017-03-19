struct test
{
    int a;
    int b;
};

void main()
{
    int a[10];
    
    for (int i = 0; i < 10; i++)
    {
        a[i] = 10 - i;
    }
    
    for (int i = 0; i < 10; i++)
    {
        printf("%d\n",a[i]);
    }
}
