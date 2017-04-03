double guess(double x, double c)
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

    printf("sqrt(%f) = %f\n", num, ans);
}
