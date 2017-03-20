int partition( int *a, int l, int r) {
    int pivot, i, j, t;
    pivot = a[l];
    i = l; j = r+1;
    
    while( 1)
    {
        do ++i; while( a[i] <= pivot && i <= r );
        do --j; while( a[j] > pivot );
        if( i >= j ) break;
        t = a[i]; a[i] = a[j]; a[j] = t;
    }
    t = a[l]; a[l] = a[j]; a[j] = t;
    return j;
}

void quickSort( int *a, int l, int r)
{
    int j;
    
    if( l < r )
    {
        // divide and conquer
        j = partition( a, l, r);
        quickSort( a, l, j-1);
        quickSort( a, j+1, r);
    }
    
}

void main()
{
    int a[9];
    a[0] = 7;
    a[1] = 12;
    a[2] = 1;
    a[3] = -2;
    a[4] = 0;
    a[5] = 15;
    a[6] = 4;
    a[7] = 11;
    a[8] = 9;
    
    int i;
    printf("Unsorted array is ");
    for(i = 0; i < 9; ++i)
    printf(" %d ", a[i]);
    
    printf("\n");
    quickSort( a, 0, 8);
    
    printf("Sorted array is  ");
    for(i = 0; i < 9; ++i)
    printf(" %d ", a[i]);
    printf("\n");
    
}
