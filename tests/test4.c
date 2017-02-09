extern void printf(char *f, ...);

void moo(int a, int *b)
{
    *b = a;
}

int main(void) {
  int x;
  int *y;

  x=1;
  y=&x;
  printf("Address of x = %d, value of x = %d\n", &x, x);
  printf("Address of y = %d, value of y = %d, value of *y = %d\n", &y, y, *y);
  moo(9,y);
}
