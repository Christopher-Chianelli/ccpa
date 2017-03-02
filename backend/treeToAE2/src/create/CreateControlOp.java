package create;

import org.w3c.dom.NodeList;

import run.TreeToAE2;

public class CreateControlOp {
	private static int labelCount = 0;
	
	public static void ifStatement(NodeList children, String regA)
	{
		System.out.println("/");
		System.out.println("L[ZERO]");
		System.out.printf("L[%s]\n",regA);
		System.out.println("CF?1");
		System.out.println("CF+1");
		
		int myLabel = createLabel(1);
		System.out.printf("J[.$L%d]\n",myLabel);
		
		TreeToAE2.printNode(children.item(0),"");
		
		System.out.printf(".$L%d\n",myLabel);
	}
	
	public static int createLabel(int i)
	{
		labelCount += i;
		return labelCount - i;
	}
	
	public static void circuitAndStatement(NodeList children, String out, String regA)
	{
		System.out.printf("N[%s] 0\n",out);
		
		TreeToAE2.printNode(children.item(1),regA);
		
		System.out.println("/");
		System.out.println("L[ZERO]");
		System.out.printf("L[%s]\n",regA);
		System.out.println("CF?1");
		System.out.println("CF+1");
		
		int myLabel = createLabel(1);
		System.out.printf("J[.$L%d]\n",myLabel);
		
		TreeToAE2.printNode(children.item(0),regA);
		System.out.println("/");
		System.out.println("L[ZERO]");
		System.out.printf("L[%s]\n",regA);
		System.out.println("CF?1");
		System.out.println("CF+1");
		System.out.printf("J[.$L%d]\n",myLabel);
		System.out.printf("N[%s] 1\n",out);
		
		System.out.printf(".$L%d\n",myLabel);
	}
	
	public static void circuitOrStatement(NodeList children, String out, String regA)
	{
		System.out.printf("N[%s] 1\n",out);
		
		TreeToAE2.printNode(children.item(1),regA);
		CreateMathOp.not(regA, regA);
		
		System.out.println("/");
		System.out.println("L[ZERO]");
		System.out.printf("L[%s]\n",regA);
		System.out.println("CF?1");
		System.out.println("CF+1");
		
		int myLabel = createLabel(1);
		System.out.printf("J[.$L%d]\n",myLabel);
		
		TreeToAE2.printNode(children.item(0),regA);
		CreateMathOp.not(regA, regA);
		System.out.println("/");
		System.out.println("L[ZERO]");
		System.out.printf("L[%s]\n",regA);
		System.out.println("CF?1");
		System.out.println("CF+1");
		System.out.printf("J[.$L%d]\n",myLabel);
		System.out.printf("N[%s] 0\n",out);
		
		System.out.printf(".$L%d\n",myLabel);
	}
	
	public static void ifStatementWithElse(NodeList children, String regA)
	{
		System.out.println("/");
		System.out.println("L[ZERO]");
		System.out.printf("L[%s]\n",regA);
		System.out.println("CF?1");
		
		int myLabel = createLabel(2);
		System.out.printf("J[.$L%d]\n",myLabel);
		
		TreeToAE2.printNode(children.item(0),"");
		
		System.out.printf("J[.$L%d]\n",myLabel + 1);
		System.out.printf(".$L%d\n",myLabel);
		
		TreeToAE2.printNode(children.item(1),"");
		System.out.printf(".$L%d\n",myLabel + 1);
	}
	
	public static void whileStatement(NodeList children)
	{
		int myLabel = createLabel(2);
		
		System.out.printf(".$L%d\n",myLabel);
		TreeToAE2.printNode(children.item(1),"T0");
		
		System.out.println("/");
		System.out.println("L[ZERO]");
		System.out.printf("L[T0]\n");
		System.out.println("CF?1");
		System.out.println("CF+1");
		System.out.printf("J[.$L%d]\n",myLabel + 1);
		
		TreeToAE2.printNode(children.item(0),"");
		
		System.out.printf("J[.$L%d]\n",myLabel);
		System.out.printf(".$L%d\n",myLabel + 1);
	}
}
