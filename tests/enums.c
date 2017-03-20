enum colors
{
    RED = 1<<0,
    GREEN,
    BLUE = 1<<1
};

void main()
{
    enum colors c;
    c = BLUE;
    printf("%d\n",c);
    
    printf("%d\n",RED);
    printf("%d\n",GREEN);
    printf("%d\n",BLUE);
}
