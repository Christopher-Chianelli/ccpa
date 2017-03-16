float guess(float x, float c)
{
    return x/2.0 + c/(2.0*x);
}

void main()
{
    printf("Enter a number\n");
    float num = readFloat();
    float ans = num / 2.0;
    for (int i = 0; i < 100; i++){
        ans = guess(ans,num);
    }
    
    printf("sqrt(%d) = %d\n", num, ans);
}
