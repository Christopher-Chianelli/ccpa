package create;

public class CreateMathOp {
	
	public static void isLessThan(String regA, String regB, String out) {
		binaryOp("-",regA,regB,out);
		binaryOp("-",out,"MINUS_ONE",out);
		System.out.printf("L[%s]'\n", "ONE");
		System.out.printf("L[%s]\n", "ZERO");
		System.out.printf("L[%s]\n", out);
		System.out.println("CF?2");
		System.out.printf("N[%s] 1\n", out);
		System.out.println("CF+1");
		System.out.printf("N[%s] 0\n", out);
	}
	
	public static void isEqual(String regA, String regB, String out) {
		System.out.println("-");
		System.out.printf("L[%s]\n", regA);
		System.out.printf("L[%s]\n", regB);
		System.out.printf("S[%s]\n", out);
		System.out.println("/");
		System.out.printf("L[%s]\n", "ZERO");
		System.out.printf("L[%s]\n", out);
		System.out.println("CF?2");
		System.out.printf("N[%s] 0\n", out);
		System.out.println("CF+1");
		System.out.printf("N[%s] 1\n", out);
	}
	
	public static void not(String in, String out) {
		System.out.println("/");
		System.out.println("L[ZERO]");
		System.out.printf("L[%s]\n",in);
		System.out.printf("N[%s] 1\n",out);
		System.out.println("CF?1");
		System.out.printf("N[%s] 0\n",out);
	}
	
	public static void binaryOp(String op, String regA, String regB, String out)
	{
		if (op.equals("%"))
		{
			System.out.println("/");
			System.out.printf("L[%s]\n",regA);
			System.out.printf("L[%s]\n",regB);
			System.out.printf("S[%s]\n",out);
		}
		else if (op.equals("/"))
		{
			System.out.println("/");
			System.out.printf("L[%s]\n",regA);
			System.out.printf("L[%s]\n",regB);
			System.out.printf("S[%s]'\n",out);
		}
		else
		{
			System.out.println(op);
			System.out.printf("L[%s]\n",regA);
			System.out.printf("L[%s]\n",regB);
			System.out.printf("S[%s]\n",out);
		}
	}
}
