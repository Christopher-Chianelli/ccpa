int A(int m, int n)
{
    if (m == 0)
        return n + 1;
    if (m > 0 && n == 0)
        return A(m - 1, 1) + A(m - 1, 1);
    else
        return A(m - 1, A(m,n-1)) + A(m - 1, A(m,n-1));
}

void main()
{
    printf("Enter a number\n");
    int m = readInt();
    printf("Enter a number\n");
    int n = readInt();
    printf("A(%d,%d) = %d\n",m,n,A(m,n));
}
