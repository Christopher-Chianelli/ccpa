package create;

import java.util.LinkedList;

import org.w3c.dom.NodeList;

import run.TreeToAE2;

public class CreateControlOp {
	private static int labelCount = 0;
	private static LinkedList<Integer> startLabels = initList();
	private static LinkedList<Integer> endLabels = initList();
	
	private static LinkedList<Integer> initList()
	{
		LinkedList<Integer> out = new LinkedList<Integer>();
		out.add(0);
		return out;
	}
	
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
		int start = myLabel;
		int end = myLabel + 1;
		
		System.out.printf(".$L%d\n",start);
		TreeToAE2.printNode(children.item(1),"T0");
		
		System.out.println("/");
		System.out.println("L[ZERO]");
		System.out.printf("L[T0]\n");
		System.out.println("CF?1");
		System.out.println("CF+1");
		System.out.printf("J[.$L%d]\n",end);
		
		startLabels.push(start);
		endLabels.push(end);
		TreeToAE2.printNode(children.item(0),"");
		startLabels.pop();
		endLabels.pop();
		
		System.out.printf("J[.$L%d]\n",start);
		System.out.printf(".$L%d\n",end);
	}
	
	public static void doWhileStatement(NodeList children)
	{
		int myLabel = createLabel(3);
		int start = myLabel;
		int test = myLabel + 1;
		int end = myLabel + 2;
		
		System.out.printf(".$L%d\n",start);
		
		startLabels.push(test);
		endLabels.push(end);
		TreeToAE2.printNode(children.item(0),"");
		startLabels.pop();
		endLabels.pop();
		
		System.out.printf(".$L%d\n",test);
		TreeToAE2.printNode(children.item(1),"T0");
		System.out.println("/");
		System.out.println("L[ZERO]");
		System.out.printf("L[T0]\n");
		System.out.println("CF?1");
		System.out.printf("J[.$L%d]\n",start);
		System.out.printf(".$L%d\n",end);
	}
	
	public static void forStatement(NodeList children)
	{
		int myLabel = createLabel(3);
		int start = myLabel;
		int post = myLabel + 1;
		int end = myLabel + 2;
		
		System.out.printf(".$L%d\n",start);
		TreeToAE2.printNode(children.item(2),"T0");
		
		System.out.println("/");
		System.out.println("L[ZERO]");
		System.out.printf("L[T0]\n");
		System.out.println("CF?1");
		System.out.println("CF+1");
		System.out.printf("J[.$L%d]\n",end);
		
		startLabels.push(post);
		endLabels.push(end);
		TreeToAE2.printNode(children.item(1),"");
		startLabels.pop();
		endLabels.pop();
		
		System.out.printf(".$L%d\n",post);
		TreeToAE2.printNode(children.item(0),"");
		System.out.printf("J[.$L%d]\n",start);
		System.out.printf(".$L%d\n",end);
	}
	
	public static void breakFromLoop()
	{
		System.out.printf("J[.$L%d]\n",endLabels.peek());
	}
	
	public static void continueLoop()
	{
		System.out.printf("J[.$L%d]\n",startLabels.peek());
	}
}
