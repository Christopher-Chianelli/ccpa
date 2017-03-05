void main()
{
    int number = 10;
    int *pointer = &number;
    
    printf("Enter a number!\n");
    *pointer = readInt();
    printf("You entered %d\n",number);
}
