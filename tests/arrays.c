int global;

int foo (int *a, int v)
{
    printf("before %d\n", global);
    *a = v;
    printf("after %d\n", global);
}

void main()
{
    int a0 = 0;
    int a1 = 1;
    int a2 = 2;
    int a3 = 3;
    int a4 = 4;
    int a5 = 5;
    int *a = &a0;
    
    for (int i = 0; i < 6; i++)
    {
        printf("Enter a number\n");
        *(a + i) = readInt();
    }
    
    for (int i = 0; i < 6; i++)
    {
        printf("%d\n",*(a + i));
    }
}
