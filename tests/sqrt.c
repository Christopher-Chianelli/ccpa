float fun(float x, float num)
{
    return x*x - num;
}

float der(float x)
{
    return 2.0*x;
}

float guess(float old, float num)
{
    float fx = fun(old,num);
    float dx = der(old);
    return old - fx/dx;
}

void main()
{
    printf("%f\n",1.0 + (-2.0));
    printf("Enter a number\n");
    float num = readFloat();
    float answer = num;

    for (int i = 0; i < 10; i++)
    {
        answer = guess(answer,num);
    }

    printf("sqrt(%f) = %f\n",num,answer);
}
