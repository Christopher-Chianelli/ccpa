union number
{
    float f;
    int i;
};

void main()
{
    union number num;
    printf("size %d\n", sizeof(union number));
    num.i = 10;
    printf("int %d\n", num.i);
    printf("float %f\n", num.f);
    
    num.f = 5.0;
    printf("int %d\n", num.i);
    printf("float %f\n", num.f);
}
