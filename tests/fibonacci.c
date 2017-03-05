int fib(int n)
{
    if (n <= 1)
        return n;
    else
        return fib(n-1) + fib(n-2);
}

void main()
{
    printf("Enter a number\n");
    int n = readInt();
    printf("fib(%d) = %d\n",n,fib(n));
}
