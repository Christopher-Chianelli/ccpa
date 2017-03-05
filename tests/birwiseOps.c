void main()
{
    printf("Enter a number\n");
    int a = readInt();
    printf("Enter a number\n");
    int b = readInt();
    
    a += b;
    
    printf("%d + %d = %d\n",a,b,a);
}
