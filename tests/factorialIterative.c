void main()
{
    printf("Enter a number\n");
    int num = readInt();
    int product = 1;
    for (int i = 1; i <= num; i++)
    {
        product *= i;
    }
    printf("%d! = %d\n",num,product);
}
