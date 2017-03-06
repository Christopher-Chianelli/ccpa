void main()
{
    int i = 0;
    do
    {
        if (i % 2 == 0)
        {
            i++;
            continue;
        }
        else
        {
            printf("%d\n",i);
            i++;
        }
    }while(i < 10);
}
