package create;

import java.math.BigInteger;

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
	
	public static void floatBinaryOp(String op, String regA, String regB, String out)
	{
		getExpPart(regA,"EXP0");
		getExpPart(regB,"EXP1");
		getDecPart(regA,"DEC0");
		getDecPart(regB,"DEC1");
		
		if (op.equals("+") || op.equals("-"))
		{
			shiftFloatsToMatch();
			
			System.out.println("/");
			System.out.println("L[DEC0]");
			System.out.println("L[TEN]");
			System.out.println("S[DEC0]'");
			
			System.out.println("L[DEC1]");
			System.out.println("L[TEN]");
			System.out.println("S[DEC1]'");
			
			binaryOp(op,"DEC0","DEC1","TEMP");
			
			System.out.println("/");
			System.out.println("L[TEMP]");
		    System.out.println("L[TEN]");
			System.out.println("S[TEMP]'");
			
			putDecPart("TEMP",out,"OTHER");
			
			binaryOp("-","OTHER","FIFTY","OTHER");
			binaryOp("-","EXP0","ONE","EXP0");
			binaryOp("+","EXP0","OTHER","EXP0");
			putExpPart("EXP0",out);
		}
		else if (op.equals("*"))
		{	
			System.out.println("*");
			System.out.println("L[DEC0]");
			System.out.println("L[DEC1]");
			System.out.println("S[DEC0]'");
			putDecPart("DEC0",out,"OTHER");
			
			//binaryOp("-", "OTHER", "FIFTY", "OTHER");
			binaryOp("+", "EXP0", "FIFTY", "EXP0");
			binaryOp("+", "EXP1", "FIFTY", "EXP1");
			binaryOp("-", "EXP0", "ONE", "EXP0");
			binaryOp("-", "EXP1", "ONE", "EXP1");
			//binaryOp("-", "OTHER", "ONE", "OTHER");
			
			binaryOp("+", "EXP0", "EXP1", "EXP0");
			//binaryOp("+", "EXP0", "OTHER", "EXP0");
			binaryOp("+", "EXP0", "FIFTY", "EXP0");
			
			putExpPart("EXP0",out);
		}
		else if (op.equals("/"))
		{
			//TODO: FIND OUT HOW TO MOVE DEC0 INTO PRIMED INGRESS AXIS FOR MORE ACCURATE DIVISION
			System.out.println("/");
			System.out.println("L[DEC1]");
			System.out.println("L[SIG]");
			System.out.println("S[DEC1]'");
			
			System.out.println("L[DEC0]");
			System.out.println("L[DEC1]");
			System.out.println("S[DEC0]'");
			
			System.out.println("*");
			System.out.println("L[DEC0]");
			System.out.println("L[SIG]");
			System.out.println("S[DEC0]");
			
			putDecPart("DEC0",out,"OTHER");
			
			//binaryOp("-", "OTHER", "FIFTY", "OTHER");
			binaryOp("+", "EXP0", "FIFTY", "EXP0");
			binaryOp("+", "EXP1", "FIFTY", "EXP1");
			binaryOp("-", "EXP0", "ONE", "EXP0");
			binaryOp("-", "EXP1", "ONE", "EXP1");
			//binaryOp("-", "OTHER", "ONE", "OTHER");
			
			binaryOp("-", "EXP0", "EXP1", "EXP0");
			//binaryOp("+", "EXP0", "OTHER", "EXP0");
			binaryOp("+", "EXP0", "FIFTY", "EXP0");
			
			putExpPart("EXP0",out);
		}
		else if (op.equals("%"))
		{
			System.out.println("/");
			System.out.println("L[DEC1]");
			System.out.println("L[SIG]");
			System.out.println("S[DEC1]'");
			
			System.out.println("L[DEC0]");
			System.out.println("L[DEC1]");
			System.out.println("S[DEC0]");
			
			System.out.println("*");
			System.out.println("L[DEC0]");
			System.out.println("L[SIG]");
			System.out.println("S[DEC0]");
			
			putDecPart("DEC0",out,"OTHER");
			putExpPart("EXP1",out);
		}
	}
	
	private static void shiftFloatsToMatch() {
		int myLabel = CreateControlOp.createLabel(3);
		int e0EQe1 = myLabel;
		int e0LTe1 = myLabel + 1;
		int e0GTe1 = myLabel + 2;
		
		isEqual("EXP0","EXP1","TEMP");
		System.out.println("/");
		System.out.println("L[ZERO]");
		System.out.println("L[TEMP]");
		System.out.println("CF?1");
		System.out.printf("J[.$L%d]\n",e0EQe1);
		
		isLessThan("EXP0","EXP1","TEMP");
		System.out.println("/");
		System.out.println("L[ZERO]");
		System.out.println("L[TEMP]");
		System.out.println("CF?1");
		System.out.printf("J[.$L%d]\n",e0LTe1);
		System.out.printf("J[.$L%d]\n",e0GTe1);
		
		System.out.printf(".$L%d\n",e0LTe1);//EXP0 < EXP1
		binaryOp("/","DEC0","TEN", "DEC0");
		binaryOp("+","EXP0","ONE", "EXP0");
		binaryOp("+","EXP0","ONE", "TEMP");
		isEqual("TEMP","EXP1","TEMP");
		System.out.println("/");
		System.out.println("L[ZERO]");
		System.out.println("L[TEMP]");
		System.out.println("CF?1");
		System.out.printf("J[.$L%d]\n",e0LTe1);
		System.out.printf("J[.$L%d]\n",e0EQe1);
		
		System.out.printf(".$L%d\n",e0GTe1);//EXP0 > EXP1
		binaryOp("/","DEC1","TEN", "DEC1");
		binaryOp("+","EXP1","ONE", "EXP1");
		binaryOp("+","EXP1","ONE", "TEMP");
		isEqual("EXP0","TEMP","TEMP");
		System.out.println("/");
		System.out.println("L[ZERO]");
		System.out.println("L[TEMP]");
		System.out.println("CF?1");
		System.out.printf("J[.$L%d]\n",e0GTe1);
		System.out.printf("J[.$L%d]\n",e0EQe1);
		
		System.out.printf(".$L%d\n",e0EQe1);//EXP0 = EXP1
	}

	public static void bitwiseOp(String op, String regA, String regB, String out)
	{
		BigInteger powerOfTwo = new BigInteger("1");
		powerOfTwo = powerOfTwo.shiftLeft(165);
		int label = CreateControlOp.createLabel(1);
		
		System.out.printf("N[%s] 0\n", out);
		
		twoComplement(regA,"DIRTY");
		twoComplement(regB,"OTHER");
		
		System.out.printf("N[CONST] %s\n", powerOfTwo.toString());
		
		System.out.printf(".$L%d\n", label);
		isLessThan("DIRTY","CONST","EXP0");
		not("EXP0","EXP0");
		System.out.println("/");
		System.out.printf("L[%s]\n", "ZERO");
		System.out.printf("L[%s]\n", "EXP0");
		System.out.println("CF?4");
		binaryOp("-","DIRTY","CONST","DIRTY");
			
		isLessThan("OTHER","CONST","EXP1");
		not("EXP1","EXP1");
		System.out.println("/");
		System.out.printf("L[%s]\n", "ZERO");
		System.out.printf("L[%s]\n", "EXP1");
		System.out.println("CF?4");
		binaryOp("-","OTHER","CONST","OTHER");
			
		if (op.equals("AND-bitwise"))
		{
			binaryOp("*","EXP0","EXP1","DEC0");
			not("DEC0","DEC0");
			not("DEC0","DEC0");
			binaryOp("*","CONST","DEC0","DEC1");
			binaryOp("+",out,"DEC1",out);
		}
		else if (op.equals("OR-bitwise"))
		{
			binaryOp("+","EXP0","EXP1","DEC0");
			not("DEC0","DEC0");
			not("DEC0","DEC0");
			binaryOp("*","CONST","DEC0","DEC1");
			binaryOp("+",out,"DEC1",out);
		}
		else if (op.equals("^"))
		{
			binaryOp("-","EXP0","EXP1","DEC0");
			not("DEC0","DEC0");
			not("DEC0","DEC0");
			binaryOp("*","CONST","DEC0","DEC1");
			binaryOp("+",out,"DEC1",out);
		}
				
		System.out.println("/");
		binaryOp("/","CONST","TWO","CONST");
		System.out.printf("L[%s]\n", "ZERO");
		System.out.printf("L[%s]\n", "CONST");
		System.out.println("CF?1");
		System.out.printf("J[.$L%d]\n", label);
		//LOOP END
		
		isLessThan(regA,"ZERO","EXP0");
		System.out.println("/");
		System.out.printf("L[%s]\n", "ZERO");
		System.out.printf("L[%s]\n", "EXP0");
		System.out.println("CF?2");
		System.out.println("N[EXP0] 1");
		System.out.println("CF+1");
		System.out.println("N[EXP0] 0");
		
		isLessThan(regB,"ZERO","EXP1");
		System.out.println("/");
		System.out.printf("L[%s]\n", "ZERO");
		System.out.printf("L[%s]\n", "EXP1");
		System.out.println("CF?2");
		System.out.println("N[EXP1] 1");
		System.out.println("CF+1");
		System.out.println("N[EXP1] 0");
		
		if (op.equals("AND-bitwise"))
		{
			binaryOp("*","EXP0","EXP1","DIRTY");
		}
		else if (op.equals("OR-bitwise"))
		{
			binaryOp("+","EXP0","EXP1","DIRTY");
		}
		else if (op.equals("^"))
		{
			binaryOp("-","EXP0","EXP1","DIRTY");
		}
		
		not("DIRTY","DIRTY");
		not("DIRTY","DIRTY");
		
		System.out.println("/");
		System.out.printf("L[%s]\n", "ZERO");
		System.out.printf("L[%s]\n", "DIRTY");
		System.out.println("CF?4");
		binaryOp("-","ZERO",out,out);
		signedTwoComplement(out,"DIRTY");
		CreateMemoryOp.moveToRegister("DIRTY", out);
	}
	
	private static void abs(String src, String dst)
	{
		isLessThan(src,"ZERO","TEMP");
		not("TEMP","TEMP");
		System.out.println("/");
		System.out.printf("L[%s]\n", "ZERO");
		System.out.printf("L[%s]\n", "TEMP");
		System.out.println("CF?5");
		binaryOp("+","ZERO",src,dst);
		System.out.println("CF+4");
		binaryOp("-","ZERO",src,dst);
	}
	
	private static void sign(String src, String dst)
	{
		isLessThan(src,"ZERO","TEMP");
		not("TEMP","TEMP");
		System.out.println("/");
		System.out.printf("L[%s]\n", "ZERO");
		System.out.printf("L[%s]\n", "TEMP");
		System.out.println("CF?2");
		System.out.printf("N[%s] 1\n", dst);
		System.out.println("CF+1");
		System.out.printf("N[%s] -1\n", dst);
	}
	
	public static void onesComplement(String src, String dst)
	{
		BigInteger powerOfTwo = new BigInteger("1");
		powerOfTwo = powerOfTwo.shiftLeft(165);
		int label = CreateControlOp.createLabel(1);
		
		isLessThan(src,"ZERO",dst);
		System.out.println("/");
		System.out.printf("L[%s]\n", "ZERO");
		System.out.printf("L[%s]\n", "DIRTY");
		System.out.println("CF?4");
		binaryOp("-","ZERO",src,dst);
		System.out.println("CF+4");
		CreateMemoryOp.moveToRegister(src, dst);
		CreateMemoryOp.moveToRegister(dst, "DEC0");
		System.out.printf("N[%s] 0\n", dst);
		
		System.out.printf("N[CONST] %s\n", powerOfTwo.toString());
		
		System.out.printf(".$L%d\n", label);
		isLessThan("DEC0","CONST","EXP0");
		not("EXP0","EXP0");
		System.out.println("/");
		System.out.printf("L[%s]\n", "ZERO");
		System.out.printf("L[%s]\n", "EXP0");
		System.out.println("CF?5");
		binaryOp("-","DEC0","CONST","DEC0");
		System.out.println("CF+4");
		binaryOp("+",dst,"CONST",dst);
		
		binaryOp("/","CONST","TWO","CONST");
		System.out.printf("L[%s]\n", "ZERO");
		System.out.printf("L[%s]\n", "CONST");
		System.out.println("CF?1");
		System.out.printf("J[.$L%d]\n", label);
	}
	
	public static String toFloat(String number)
	{
		int dot = number.indexOf('.');
		int power = dot + 49;
		
		if (dot == 1 && number.charAt(0) == '0')
		{
			do
			{
				dot++;
				power--;
			}
			while(dot < number.length() && number.charAt(dot) == '0');
		}
		
		if (dot == number.length())
			power = 0;
		
		String powerString = String.format("%d", power);
		String decimalString = String.format("%-48s", number.replace(".", "")).replace(' ', '0');
		
		return powerString + decimalString;
	}
	
	public static void getExpPart(String in, String out)
	{
		System.out.println("/");
		System.out.printf("L[%s]\n",in);
		System.out.println("L[POW]");
		System.out.printf("S[%s]'\n",out);
		binaryOp("+","ZERO",out,out);
		abs(out,out);
	}
	
	public static void getDecPart(String in, String out)
	{
		System.out.println("*");
		System.out.printf("L[%s]\n",in);
		System.out.println("L[TEN]");
		System.out.printf("S[%s]\n",out);
		System.out.printf("L[%s]\n",out);
		System.out.println("L[TEN]");
		System.out.printf("S[%s]\n",out);
	}
	
	public static void putDecPart(String src, String dec, String exp)
	{
		System.out.printf("N[%s] 50\n",exp);
		CreateMemoryOp.moveToRegister(src, dec);
		
		int label = CreateControlOp.createLabel(1);
		
		System.out.printf(".$L%d\n",label);
		abs(dec,"DIRTY");
		isLessThan("POW","DIRTY","DIRTY");
		System.out.println("/");
		System.out.println("L[ZERO]");
		System.out.println("L[DIRTY]");
		System.out.println("CF?10");
		System.out.println("CF+1");
		binaryOp("/",dec,"TEN",dec);
		binaryOp("+",exp,"ONE",exp);
		System.out.printf("J[.$L%d]\n",label);
	}
	
	public static void putExpPart(String src, String dst)
	{
		sign(dst,"DIRTY");
		binaryOp("*",src,"DIRTY","DIRTY");
		binaryOp("*","DIRTY","POW","DIRTY");
		binaryOp("+","DIRTY",dst,dst);
	}
	
	private static void twoComplement(String src, String dst)
	{
		int label = CreateControlOp.createLabel(1);
		
		isLessThan(src,"ZERO",dst);
		not(dst,dst);
		System.out.println("/");
		System.out.printf("L[%s]\n", "ZERO");
		System.out.printf("L[%s]\n", dst);
		System.out.println("CF?5");
		CreateMemoryOp.moveToRegister(src, dst);
		System.out.printf("J[.$L%d]\n",label);
		onesComplement(src,dst);
		binaryOp("+",dst,"ONE",dst);
		System.out.printf(".$L%d\n",label);
	}
	
	private static void signedTwoComplement(String src, String dst)
	{
		int label = CreateControlOp.createLabel(1);
		
		isLessThan(src,"ZERO",dst);
		not(dst,dst);
		System.out.println("/");
		System.out.printf("L[%s]\n", "ZERO");
		System.out.printf("L[%s]\n", dst);
		System.out.println("CF?5");
		CreateMemoryOp.moveToRegister(src, dst);
		System.out.printf("J[.$L%d]\n",label);
		onesComplement(src,dst);
		binaryOp("+",dst,"ONE",dst);
		binaryOp("-","ZERO",dst,dst);
		System.out.printf(".$L%d\n",label);
	}
	
}
