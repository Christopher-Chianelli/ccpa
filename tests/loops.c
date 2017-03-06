void main()
{
    int count = 0;
    for (;;)
    {
        if (count == 10)
        break;
        printf("A%d\n",count);
        count++;
    }
    
    for (int i = 0;;)
    {
        if (i == 10)
        break;
        printf("B%d\n",i);
        i++;
    }
    
    count = 0;
    for (;count < 10;)
    {
        printf("C%d\n",count);
        count++;
    }
    
    count = 0;
    for (;;count++)
    {
        if (count == 10)
        break;
        printf("D%d\n",count);
    }
    
    for (int i = 0;i < 10;)
    {
        printf("E%d\n",i);
        i++;
    }
    
    for (int i = 0;;i++)
    {
        if (i == 10)
        break;
        printf("F%d\n",i);
    }
    
    count = 0;
    for (;count < 10;count++)
    {
        printf("G%d\n",count);
    }
    
    for (int i = 0; i < 10; i++)
    {
        printf("H%d\n",i);
    }
    
    while(0){printf("I\n");}
    do{printf("J\n");}while(0);
    
    for (int i = 0; i < 10; i++)
    {
        if (i == 0)
        continue;
        printf("K%d\n",i);
    }
    
    count = 0;
    do{count++;if(count == 1) continue; printf("L\n");}while(0);
}
