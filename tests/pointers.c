int global;

int foo (int *a, int v)
{
    printf("before %d\n", global);
    *a = v;
    printf("after %d\n", global);
}

void main()
{
    int overflow = 0;
    int *a, **b;
    a = &global;
    b = &a;
    foo(a,10);
    
    int c;
    **b -= 5;
    
    a = &c;
    foo(a,2);
    
    *(&a - 1) = 20;
    printf("global %d\n", global);
    printf("a %d\n", a);
    printf("b %d\n", b);
    printf("*a %d\n", a);
    printf("*b %d\n", *b);
    printf("**b %d\n", **b);
    printf("c %d\n", c);
     printf("overflow %d\n", overflow);
}
