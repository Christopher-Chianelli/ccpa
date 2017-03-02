extern float readFloat();
extern void printf(char *,...);

float fun(float a)
{
    return a*a;
}

float der(float a)
{
    return 2.0*a;
}

float guess(float *a, float *b)
{
    float m = (a + b)/2.0;
    if (fun(m) < 10.0)
    {
        *a = m;
    }
    else
    {
        *b = m;
    }
    return m;
}

void main()
{
    float a = readFloat();
    float start,end;
    
    start = 0.0;
    end = a;
    float answer;
    
    for (int i = 0; i < 10; i++)
    {
        answer = guess(&start,&end);
    }
    
    printf("%f\n",answer);
}
