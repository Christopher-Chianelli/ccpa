enum date
{
    DAY,MONTH = 5,YEAR
};

void main()
{
    enum date me;
    me = DAY;
    printf("%d\n",me);
    printf("%d\n",MONTH);
    printf("%d\n",YEAR);
}
