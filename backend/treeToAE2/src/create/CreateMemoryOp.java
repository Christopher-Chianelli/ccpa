package create;

import java.util.LinkedList;

import org.w3c.dom.Node;

public class CreateMemoryOp {
	private static LinkedList<Integer> argOffsets = init();
	
	private static LinkedList<Integer> init()
	{
		LinkedList<Integer> out = new LinkedList<Integer>();
		out.add(0);
		return out;
	}
	
	public static void storeAtAddress(String valueR)
	{
		System.out.println("+");
		System.out.println("L[ZERO]");
		System.out.println("L[ZERO]");
		System.out.println("S[DIRTY]");
		System.out.println("L[ZERO]");
		System.out.println("L[MEMADD]");
		System.out.println("A replace next card with result from the Egress Axis");
		System.out.println("S000");
		
		System.out.println("/");
		System.out.println("L[ZERO]");
		System.out.println("L[DIRTY]");
		System.out.println("CF?1");
		System.out.println("CF+7");
		
		System.out.println("+");
		System.out.println("L[ZERO]");
		System.out.println("L[ONE]");
		System.out.println("S[DIRTY]");
		
		System.out.printf("L[%s]\n",valueR);
		System.out.println("L[ZERO]");
		System.out.println("CB+13");
	}
	
	public static void readFromAddress(String dstR)
	{
		System.out.println("+");
		System.out.println("L[ZERO]");
		System.out.println("L[MEMADD]");
		System.out.println("A replace next card with result from the Egress Axis");
		System.out.println("L000");
		System.out.println("L[ZERO]");
		System.out.printf("S[%s]\n",dstR);
	}
	
	public static void getAddressFromStack(String offsetR)
	{
		System.out.println("-");
	    System.out.println("L[STACK_TOP]");
	    System.out.printf("L[%s]\n", offsetR);
	    System.out.println("S[MEMADD]");
	}
	
	public static void getGlobalAddress(String address)
	{
		System.out.printf("N[MEMADD] %s\n", address);
	}
	
	public static void getVariableFromStack(String variableNum)
	{
		System.out.printf("N[%s] %s\n", "DIRTY", variableNum);
		getAddressFromStack("DIRTY");
	}
	
	public static void pushArgToStack(String argR)
	{
		int argOffset = argOffsets.pop().intValue();
		
		System.out.printf("N[%s] %d\n", "OTHER", -argOffset);
	    getAddressFromStack("OTHER");
	    storeAtAddress(argR);
	    
		argOffsets.push(new Integer(argOffset + 1));
	}
	
	public static void moveToRegister(String src, String dst)
	{
		CreateMathOp.binaryOp("+", "ZERO", src, dst);
	}
	
	public static void createCall()
	{
		argOffsets.push(new Integer(argOffsets.peek() + 1));
	}
	
	public static void finishCall(Node node, String outR)
	{
		argOffsets.pop();
		int offset = argOffsets.peek();
		System.out.printf("N[DIRTY] %d\n", offset);
		System.out.println("+");
	    System.out.println("L[STACK_TOP]");
	    System.out.println("L[DIRTY]");
	    System.out.println("S[STACK_TOP]");
	    
	    System.out.println("A notes location of next card on stack");
		System.out.println("CF?1");
		System.out.printf("J[.%s]\n",node.getFirstChild().getTextContent());
		
		System.out.printf("N[DIRTY] %d\n", offset);
		System.out.println("-");
	    System.out.println("L[STACK_TOP]");
	    System.out.println("L[DIRTY]");
	    System.out.println("S[STACK_TOP]");
	    
		System.out.println("+");
		System.out.println("L[ZERO]");
		System.out.println("L[OUT]");
		System.out.printf("S[%s]\n", outR);
		
		
	}
}
