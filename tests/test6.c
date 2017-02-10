#define NULL (0)
extern void printf(char*,...);
extern void *malloc(int);

struct linkedList
{
    int x;
    struct linkedList *l;
};

int factorial(int n)
{
    if (n == 0)
        return 1;
    else
        return n*factorial(n-1);
}

void main()
{
    struct linkedList list;
    struct linkedList *temp;

    for (int i = 0;i < 10;i++)
    {
        temp->x = i;
        temp->next = malloc(sizeof(struct linkedlist));
        temp = temp->next;
    }

    temp->x = 10;
    temp->next = NULL;
    temp = &list;

    while (temp)
    {
        printf("%d\n",factorial(temp->x));
        temp = temp->next;
    }
}
