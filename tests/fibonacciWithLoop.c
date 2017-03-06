void main()
{
    printf("Enter a number\n");
    
    int n = readInt();
    int a = 1;
    int b = 0;
    int i = 1;
    
    for (;i < n;i++)
    {
        int c = a + b;
        b = a;
        a = c;
    }
    printf("fib(%d) = %d\n",n,a);
}
