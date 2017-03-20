#define NULL 0

int hp;

void *malloc(int size)
{
    hp += size;
    return &hp - hp;
}

struct linkedList
{
    int val;
    struct linkedList *next;
};

void main()
{
    struct linkedList list, *temp;
    hp = 0;
    list.val = 0;
    list.next = NULL;
    
    temp = &list;
    int input;
    
    printf("Enter a list of numbers. Enter 0 to end. \n");
    while (input = readInt())
    {
        struct linkedList *node = malloc(sizeof(struct linkedList));
        node->val = input;
        node->next = NULL;
        temp->next = node;
        temp = temp->next;
    }
    
    printf("\n");
    temp = list.next;
    while(temp)
    {
        printf("%d\n", temp->val);
        temp = temp->next;
    }
}
