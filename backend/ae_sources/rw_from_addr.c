//REGADD (Memory address to load)
//REGVAL (Register to store value from that memory address)
//RW (0 if read, 1 if write)
//RESERVED (Work memory)
//TWO (Stores constant value 2)
#include <stdio.h>

#define P(s) printf(#s "\n")

static void print_op(int addr) {
    if (addr >= 1000) {
        printf("H Segmentation Fault\n");
    }
    else
    {
        P(/);
        P(L[ZERO]);
        P(L[RW]);
        P(CF?4);

        P(L[REGVAL]);
        P(L[ONE]);
        printf("S%d'\n", addr);
        P(J[.$returnToCaller]);

        printf("L%d\n", addr);
        P(L[ONE]);
        printf("S[REGVAL]'\n");
        P(J[.$returnToCaller]);
    }
}

static int length_of_parts(int pow_of_two, int mem_addr) {
    if (!pow_of_two) {
        if (mem_addr >= 1000) {
            return 1;
        }
        else {
            return 12;
        }
    }
    else {
        return 15 + length_of_parts(pow_of_two >> 1, mem_addr) + length_of_parts(pow_of_two >> 1, mem_addr - pow_of_two);
    }
}

static void print_parts(int pow_of_two, int max_addr) {
    if (!pow_of_two) {
        print_op(max_addr);
        return;
    }
    printf("N[RESERVED] %d\n", pow_of_two);
    P(L[REGADD]);
    P(L[RESERVED]);
    P(S[RESERVED]);
    P(L[RESERVED]);
    P(L[MINUS_ONE]);
    P(S[RESERVED]);
    printf("L[ONE]'\n");
    P(L[ZERO]);
    P(L[RESERVED]);
    printf("CF?%d\n", length_of_parts(pow_of_two >> 1, max_addr));
    //REGADD < pow_of_two
    print_parts(pow_of_two >> 1, max_addr - pow_of_two);
    //REGADD >= pow_of_two
    printf("N[RESERVED] %d\n", pow_of_two);
    P(L[REGADD]);
    P(L[RESERVED]);
    P(S[REGADD]);
    print_parts(pow_of_two >> 1, max_addr);

}

int main() {
    P(.$rwFromAddr);
    P(-);
    print_parts(512,1023);
}
