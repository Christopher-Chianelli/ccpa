int factorial(int n)
{
    if (n <= 0)
        return 1;
    else
        return factorial(n-1)*n;
}

void main()
{
    printf("Enter a number\n");
    int num = readInt();
    printf("%d! = %d\n",num,factorial(num));
}
