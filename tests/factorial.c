int factorial(int n)
{
    if (n <= 1)
        return 1;
    else
        return n*factorial(n-1);
}

void main()
{
    printf("Enter a number\n");
    int n = readInt();
    printf("%d! = %d\n",n,factorial(n));
}
