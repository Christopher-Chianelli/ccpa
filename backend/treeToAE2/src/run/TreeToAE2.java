/*
 * backend/treeToAE2/src/run/TreeToAE2.java
 * Copyright (C) 2017 Christopher Chianelli
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package run;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import create.CreateControlOp;
import create.CreateLibraryFunction;
import create.CreateMathOp;
import create.CreateMemoryOp;
import transformer.TreeTransformer;


public class TreeToAE2 {
	public static Document doc;
	public static LinkedList<String> myStrings;
	private static String myStackSize = "";
	private static HashMap<String,Integer> structSize = new HashMap<String,Integer>();
	private static HashMap<String, Integer> structIndex = new HashMap<String,Integer>();

	private static Document readFromInput() throws Exception
	{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		return docBuilder.parse(System.in);
	}

	private static void printProgram(Node root) {
		NodeList init = root.getFirstChild().getChildNodes();
		NodeList functions = root.getFirstChild().getNextSibling().getChildNodes();

		System.out.printf(".%d\n", TreeTransformer.numOfTempRegisters);
		System.out.println("A write in columns");
		System.out.println("N[ZERO] 0");
		System.out.println("N[ONE] 1");
		System.out.println("N[MINUS_ONE] -1");
		System.out.println("N[TWO] 2");
		System.out.println("N[TEN] 10");
		System.out.println("N[POW] " + new BigInteger("10").pow(48));
		System.out.println("N[SIG] " + new BigInteger("10").pow(24));
		System.out.println("N[FIFTY] 50");
		System.out.println("N[STACK_TOP] 0");
		System.out.println("N[MAX_NUM] 99999999999999999999999999999999999999999999999999");
		System.out.println("N[MIN_NUM] -99999999999999999999999999999999999999999999999999");

		for (int i = 0; i < init.getLength(); i++)
		{
			printNode(init.item(i),"");
		}

		for (int i = 0; i < functions.getLength(); i++)
		{
			printNode(functions.item(i),"");
		}

		CreateLibraryFunction.defineReadInt();
		CreateLibraryFunction.defineReadFloat();
		CreateLibraryFunction.definePrintNewLine();
		CreateLibraryFunction.definePrintInt();
		CreateLibraryFunction.definePrintFloat();
		CreateLibraryFunction.definePrintString();
		CreateLibraryFunction.defineReturnFunction();
	}

	private static void returnFromFunction()
	{
		//System.out.printf("N[DIRTY] %s\n", myStackSize);
		//System.out.println("-");
		//System.out.println("L[STACK_TOP]");
		//System.out.println("L[DIRTY]");
		//System.out.println("S[STACK_TOP]");
		returnToCaller();
	}

	public static void returnToCaller()
	{
		CreateMemoryOp.moveToRegister("STACK_TOP", "MEMADD");
		CreateMemoryOp.readFromAddress("RETURN_ADDR");
		System.out.println("J[.$returnToCaller]");
		//System.out.println("L[ZERO]");
		//System.out.printf("L[ZERO]\n");
		//System.out.println("A returns to location on the top of the stack");
	}

	public static void printNode(Node node, String register)
	{
		if (node == null)
			return;

		String regA;
		String regB;
		String outR;

		if (register.startsWith("T"))
		{
			regA = "T" + (Integer.parseInt(register.substring(1)) + 1);
			regB = "T" + (Integer.parseInt(register.substring(1)) + 2);
		}
		else
		{
			regA = "T0";
			regB = "T1";
		}

		if (register.isEmpty())
		{
			outR = "T0";
		}
		else
		{
			outR = register;
		}

		if (node.getNodeName().equals("value"))
		{
			String address = getAddress(node.getTextContent(),node);
			String type = getTypeOf(node);

			if (address == null)
				return;

			if (address.startsWith("V"))
			{
				CreateMemoryOp.getVariableFromStack(address.substring(1));
			}
			else
			{
				CreateMemoryOp.getGlobalAddress(address.substring(1));
			}

			if (type.endsWith("]"))
			{
			    CreateMemoryOp.moveToRegister("MEMADD", outR);
			    System.out.printf("N[DIRTY] %d\n", getSizeOf(type) - 1);
                CreateMathOp.binaryOp("-", outR, "DIRTY", outR);
			}
			else
			{
			    CreateMemoryOp.readFromAddress(outR);
			}
		}
		else if (node.getNodeName().equals("op"))
		{
			NamedNodeMap attr = node.getAttributes();
			String operation = attr.getNamedItem("name").getTextContent();
			NodeList children = node.getChildNodes();

			if (operation.equals("HALT"))
			{
				System.out.println("H");
				return;
			}
			else if (operation.equals("FUN"))
			{
				System.out.println("." + children.item(0).getTextContent());
				//myStackSize = attr.getNamedItem("stackSize").getTextContent();
				//System.out.printf("N[T0] %s\n", myStackSize);
				//System.out.println("+");
				//System.out.println("L[STACK_TOP]");
				//System.out.println("L[T0]");
				//System.out.println("S[STACK_TOP]");

				if (attr.getNamedItem("isRecursive") != null && attr.getNamedItem("isRecursive").getTextContent().equals("false"))
				{
					for (int i = children.getLength() - 1; i >= children.getLength() - getNumberOfArguments(children.item(0).getTextContent()); i--)
					{
						loadStackValueIntoGlobalRegister(children.item(0).getTextContent(),
								children.item(i).getLastChild().getTextContent(),
								children.item(i).getAttributes().getNamedItem("address").getTextContent());
					}
				}

				printNode(children.item(1),"");
				returnFromFunction();
				return;
			}
			else if (operation.equals("GET_MEM"))
			{
				String type = attr.getNamedItem("type").getTextContent();
				printNode(children.item(0),regA);
				CreateMemoryOp.moveToRegister(regA,"MEMADD");
				if (type.endsWith("]"))
				{
					CreateMemoryOp.moveToRegister(regA,outR);
				}
				else
				{
				    CreateMemoryOp.readFromAddress(outR);
				}
				return;
			}
			else if (operation.equals("GET_MEMBER"))
			{
				printNode(children.item(1),regA);

				String type = getTypeOf(children.item(1));
				String memberType = attr.getNamedItem("type").getTextContent();
				int num = structIndex.get(type + "." + children.item(0).getTextContent());
				System.out.printf("N[%s] %d\n", regB, num);

				CreateMathOp.binaryOp("-", "MEMADD", regB, "MEMADD");

				if (memberType.endsWith("]"))
				{
				    CreateMemoryOp.moveToRegister("MEMADD", outR);
				    System.out.printf("N[DIRTY] %d\n", getSizeOf(memberType) - 1);
	                CreateMathOp.binaryOp("-", outR, "DIRTY", outR);
				}
				else
				{
				    CreateMemoryOp.readFromAddress(outR);
				}
				return;
			}
			else if (operation.equals("GET_ACCESS"))
			{
				printNode(children.item(1),regA);
				CreateMemoryOp.moveToRegister(regA,"MEMADD");

				String type = getTypeOf(children.item(1));
				String memberType = attr.getNamedItem("type").getTextContent();
				type = type.substring(0,type.length() - 1);

				int num = structIndex.get(type + "." + children.item(0).getTextContent());
				System.out.printf("N[%s] %d\n", regB, num - 1);

				CreateMathOp.binaryOp("-", "MEMADD", regB, "MEMADD");

				if (memberType.endsWith("]"))
				{
				    CreateMemoryOp.moveToRegister("MEMADD", outR);
				    System.out.printf("N[DIRTY] %d\n", getSizeOf(memberType) - 1);
	                CreateMathOp.binaryOp("-", outR, "DIRTY", outR);
				}
				else
				{
				    CreateMemoryOp.readFromAddress(outR);
				}
				return;
			}
			else if (operation.equals("SIZEOF"))
			{
				String type = getTypeOf(children.item(0));
				String num = Integer.toString(getSizeOf(type));

				System.out.printf("N[%s] %s\n", outR, num);
				return;
			}
			else if (operation.equals("ADDRESS"))
			{
				printNode(children.item(0),regA);
				CreateMemoryOp.moveToRegister("MEMADD", outR);
                String type = getTypeOf(children.item(0));
                System.out.printf("N[DIRTY] %d\n", getSizeOf(type) - 1);
                CreateMathOp.binaryOp("-", outR, "DIRTY", outR);
				return;
			}
			else if (operation.equals("CALL"))
			{
				CreateMemoryOp.createCall();
				printNode(children.item(1),regA);
				CreateMemoryOp.pushArgToStack(regA);
				CreateMemoryOp.finishCall(node,outR);
				return;
			}
			else if (operation.equals("COMBINE_ARGS"))
			{
				printNode(children.item(1), regA);
				CreateMemoryOp.pushArgToStack(regA);
				printNode(children.item(0), outR);
			}
			else if (operation.equals("RETURN"))
			{
				returnFromFunction();
				return;
			}
			else if (operation.equals("RETURN VAL"))
			{
				printNode(children.item(0),"OUT");
				returnFromFunction();
				return;
			}
			else if (operation.equals("if"))
			{
				printNode(children.item(1), regA);
				CreateControlOp.ifStatement(children, regA);
				return;
			}
			else if (operation.equals("if/else"))
			{
				printNode(children.item(2), regA);
				CreateControlOp.ifStatementWithElse(children, regA);
				return;
			}
			else if (operation.equals("?"))
			{
				printNode(children.item(2), regA);
				CreateControlOp.ternaryOp(children, regA, outR);
				return;
			}
			else if (operation.equals("while"))
			{
				CreateControlOp.whileStatement(children);
				return;
			}
			else if (operation.equals("doWhile"))
			{
				CreateControlOp.doWhileStatement(children);
				return;
			}
			else if (operation.equals("for"))
			{
				CreateControlOp.forStatement(children);
				return;
			}
			else if (operation.equals("BREAK"))
			{
				CreateControlOp.breakFromLoop();
				return;
			}
			else if (operation.equals("CONTINUE"))
			{
				CreateControlOp.continueLoop();
				return;
			}
			else if (operation.equals(";"))
			{
				printNode(children.item(1),register);
				printNode(children.item(0),register);
			}
			else if (operation.equals(","))
			{
				printNode(children.item(1),outR);
				printNode(children.item(0),outR);
			}
			else if (operation.equals("+") || operation.equals("-") || operation.equals("/") || operation.equals("*") || operation.equals("%"))
			{
				String type = attr.getNamedItem("type").getTextContent();
				printNode(children.item(1), regA);
				printNode(children.item(0), regB);
				if (type.equals("float"))
				{
				    CreateMathOp.floatBinaryOp(operation,regA,regB,outR);
				}
				else if (type.endsWith("*") || type.endsWith("]"))
				{
					String leftType = getTypeOf(children.item(1));
					String rightType = getTypeOf(children.item(0));
					String pointerType;
					if (leftType.endsWith("*") || leftType.endsWith("]"))
					{
						pointerType = leftType;
					}
					else
					{
						pointerType = rightType;
					}

					int size;
					int lastIndex = pointerType.length() - 1;
					if (pointerType.charAt(lastIndex) == '*')
					{
						lastIndex--;
					}
					else
					{
					    while(pointerType.charAt(lastIndex) == ']')
					    {
							while(pointerType.charAt(lastIndex) != '[')
							{
								lastIndex--;
							}
							lastIndex--;
						}
					}

					lastIndex++;
					String newType;

					if (pointerType.charAt(lastIndex) == '*')
					{
						newType = pointerType.substring(0, lastIndex) + pointerType.substring(lastIndex + 1);
					}
					else
					{
						int arrayEnd = lastIndex;
						while (pointerType.charAt(arrayEnd) != ']')
						{
							arrayEnd++;
						}
						newType = pointerType.substring(0, lastIndex) + pointerType.substring(arrayEnd + 1);
					}

					size = getSizeOf(newType);
					System.out.printf("N[DIRTY] %d\n", size);
					if (leftType.equals(pointerType))
					{
						CreateMathOp.binaryOp("*", regB, "DIRTY", regB);
						System.out.printf("N[DIRTY] %d\n", size - 1);
						CreateMathOp.binaryOp("+", regB,"DIRTY",regB);
					}
					else
					{
						CreateMathOp.binaryOp("*", regA, "DIRTY", regA);
						System.out.printf("N[DIRTY] %d\n", size - 1);
						CreateMathOp.binaryOp("+", regA,"DIRTY",regA);
					}

					CreateMathOp.binaryOp(operation,regA,regB,outR);
				}
				else
				{
					CreateMathOp.binaryOp(operation,regA,regB,outR);
				}

			}
			else if (operation.equals("AND-bitwise") || operation.equals("OR-bitwise") || operation.equals("^"))
			{
				printNode(children.item(1), regA);
				printNode(children.item(0), regB);
				CreateMathOp.bitwiseOp(operation,regA,regB,outR);
			}
			else if (operation.equals("LS") || operation.equals("RS"))
			{
				printNode(children.item(1), regA);
				printNode(children.item(0), regB);
				CreateMathOp.shiftOp(operation,regA,regB,outR);
			}
			else if (operation.equals("~"))
			{
				printNode(children.item(0), regB);
				CreateMathOp.bitwiseOp("^","MINUS_ONE",regB,outR);
			}
			else if (operation.equals("-U"))
			{
				String type = attr.getNamedItem("type").getTextContent();
				printNode(children.item(0), regA);
				if (type.equals("float"))
					CreateMathOp.floatBinaryOp("-","ZERO",regA,outR);
				else
					CreateMathOp.binaryOp("-","ZERO",regA,outR);

			}
			else if (operation.equals("+U"))
			{
				String type = attr.getNamedItem("type").getTextContent();
				printNode(children.item(0), regA);
				if (type.equals("float"))
					CreateMathOp.floatBinaryOp("+","ZERO",regA,outR);
				else
					CreateMathOp.binaryOp("+","ZERO",regA,outR);
			}
			else if (operation.equals("POST++"))
			{
				printNode(children.item(0), outR);
				System.out.printf("N[%s] 1\n",regB);
				CreateMathOp.binaryOp("+", outR, regB, regB);
				CreateMemoryOp.storeAtAddress(regB);
			}
			else if (operation.equals("PRE++"))
			{
				printNode(children.item(0), outR);
				System.out.printf("N[%s] 1\n",regB);
				CreateMathOp.binaryOp("+", outR, regB, outR);
				CreateMemoryOp.storeAtAddress(outR);
			}
			else if (operation.equals("POST--"))
			{
				printNode(children.item(0), outR);
				System.out.printf("N[%s] -1\n",regB);
				CreateMathOp.binaryOp("+", outR, regB, regB);
				CreateMemoryOp.storeAtAddress(regB);
			}
			else if (operation.equals("PRE--"))
			{
				printNode(children.item(0), outR);
				System.out.printf("N[%s] -1\n",regB);
				CreateMathOp.binaryOp("+", outR, regB, outR);
				CreateMemoryOp.storeAtAddress(outR);
			}
			else if (operation.equals("=") || operation.equals("ASSIGN"))
			{
				printNode(children.item(0), outR);
				printNode(children.item(1), regB);
				CreateMemoryOp.storeAtAddress(outR);
			}
			else if (operation.equals("+=") || operation.equals("-=") || operation.equals("/=") || operation.equals("*=") || operation.equals("%="))
			{
				String type = attr.getNamedItem("type").getTextContent();
				printNode(children.item(0), regA);
				printNode(children.item(1), regB);

				if (type.equals("float"))
					CreateMathOp.floatBinaryOp(operation.substring(0, 1), regB, regA, "OUT");
				else
					CreateMathOp.binaryOp(operation.substring(0, 1), regB, regA, "OUT");

				CreateMemoryOp.storeAtAddress("OUT");
				CreateMemoryOp.moveToRegister("OUT", outR);
			}
			else if (operation.equals("AND-bitwise=") || operation.equals("OR-bitwise=") || operation.equals("^="))
			{
				printNode(children.item(0), regA);
				printNode(children.item(1), regB);
				CreateMathOp.bitwiseOp(operation.substring(0,operation.length() - 1), regB, regA, "OUT");
				CreateMemoryOp.storeAtAddress("OUT");
				CreateMemoryOp.moveToRegister("OUT", outR);
			}
			else if (operation.equals("LS=") || operation.equals("RS="))
			{
				printNode(children.item(0), regA);
				printNode(children.item(1), regB);
				CreateMathOp.shiftOp(operation.substring(0,2), regB, regA, "OUT");
				CreateMemoryOp.storeAtAddress("OUT");
				CreateMemoryOp.moveToRegister("OUT", outR);
			}
			else if (operation.equals("LT"))
			{
				String type = attr.getNamedItem("type").getTextContent();
				printNode(children.item(1), regA);
				printNode(children.item(0), regB);
				if (type.equals("float"))
				    CreateMathOp.isFloatLessThan(regA,regB,outR);
				else
					CreateMathOp.isLessThan(regA,regB,outR);
				return;
			}
			else if (operation.equals("GT"))
			{
				String type = attr.getNamedItem("type").getTextContent();
				printNode(children.item(1), regA);
				printNode(children.item(0), regB);
				if (type.equals("float"))
				    CreateMathOp.isFloatLessThan(regB,regA,outR);
				else
				    CreateMathOp.isLessThan(regB,regA,outR);
				return;
			}
			else if (operation.equals("LE"))
			{
				String type = attr.getNamedItem("type").getTextContent();
				printNode(children.item(1), regA);
				printNode(children.item(0), regB);
				if (type.equals("float"))
				    CreateMathOp.isFloatLessThan(regB,regA,outR);
				else
				    CreateMathOp.isLessThan(regB,regA,outR);
				CreateMathOp.not(outR,outR);
				return;
			}
			else if (operation.equals("GE"))
			{
				String type = attr.getNamedItem("type").getTextContent();
				printNode(children.item(1), regA);
				printNode(children.item(0), regB);
				if (type.equals("float"))
				    CreateMathOp.isFloatLessThan(regA,regB,outR);
				else
				    CreateMathOp.isLessThan(regA,regB,outR);

				CreateMathOp.not(outR,outR);
				return;
			}
			else if (operation.equals("=="))
			{
				printNode(children.item(1), regA);
				printNode(children.item(0), regB);
				CreateMathOp.isEqual(regA,regB,outR);
				return;
			}
			else if (operation.equals("!="))
			{
				printNode(children.item(1), regA);
				printNode(children.item(0), regB);
				CreateMathOp.isEqual(regA,regB,outR);
				CreateMathOp.not(outR,outR);
				return;
			}
			else if (operation.equals("!"))
			{
				printNode(children.item(0), regA);
				CreateMathOp.not(regA,outR);
				return;
			}
			else if (operation.equals("AND"))
			{
				CreateControlOp.circuitAndStatement(children, outR, regA);
				return;
			}
			else if (operation.equals("OR"))
			{
				CreateControlOp.circuitOrStatement(children, outR, regA);
				return;
			}
			else if (operation.equals("STRING"))
			{
				String string = children.item(0).getTextContent();
				int index = myStrings.indexOf(string);
				if (index == -1)
				{
					index = myStrings.size();
					myStrings.addLast(string);
				}
				System.out.printf("N[%s] %d\n", outR, index);
			}
			else if (operation.equals("INT"))
			{
				String num = children.item(0).getTextContent();
				System.out.printf("N[%s] %s\n", outR, num);
			}
			else if (operation.equals("FLOAT"))
			{
				String num = children.item(0).getTextContent();
				System.out.printf("N[%s] %s\n", outR, CreateMathOp.toFloat(num));
			}
		}
		return;
	}

	private static int getNumberOfArguments(String function) {
		Node variable = findVariable(function,doc.getFirstChild());
		String type = variable.getFirstChild().getTextContent();
		int numberOfBrackets = 0;
		int i = type.length() - 1;

		do
		{
			if (type.charAt(i) == ')')
			{
				numberOfBrackets++;
			}
			else if (type.charAt(i) == '(')
			{
				numberOfBrackets--;
			}

			i--;
		}while(numberOfBrackets > 0);

		return (type.substring(i).length() > 3)? type.substring(i).split(",").length : 0;
	}

	private static void loadStackValueIntoGlobalRegister(String function, String parameter, String parameterNumber) {
		CreateMemoryOp.getVariableFromStack(parameterNumber.substring(1));
		CreateMemoryOp.readFromAddress("T0");
		System.out.printf("+\n");
		System.out.printf("L[ZERO]\n");
		System.out.printf("L[T0]\n");
		System.out.printf("S%s\n",getAddress(function + ":" + parameter, null).substring(1));
	}

	private static String getAddress(String variable, Node node) {
		if (node == null)
		{
			Node last = doc.getFirstChild().getLastChild().getLastChild();
			while (last != null)
			{
				if (last.getNodeName().equals("uses"))
				{
					if (last.getLastChild().getTextContent().equals(variable))
					{
						NamedNodeMap attr = last.getAttributes();
						return attr.getNamedItem("address").getTextContent();
					}
				}
				last = last.getPreviousSibling();
			}
			return null;
		}
		else
		{
			Node last = node.getLastChild();
			while (last != null)
			{
				if (last.getNodeName().equals("uses") && last.getLastChild().getTextContent().equals(variable))
				{
					NamedNodeMap attr = last.getAttributes();
					return attr.getNamedItem("address").getTextContent();
				}
				last = last.getPreviousSibling();
			}
			return getAddress(variable,node.getParentNode());
		}
	}

	public static void main(String[] args) throws Exception {
		try
		{
		    doc = readFromInput();
		    myStrings = new LinkedList<String>();
		    Node root = doc.getFirstChild();
		    TreeTransformer.moveFunctionCalls(root);
		    TreeTransformer.convertPrintf(root);
		    TreeTransformer.setStructSizes(root);
		    TreeTransformer.setStructIndices(root);
		    TreeTransformer.getMaxRequiredRegisters(root);
		    if (args.length > 0 && args[0].equals("y"))
		    	TreeTransformer.giveFunctionsGlobalAddresses(root);
		    TreeTransformer.giveVariablesAddresses(root);
		    printProgram(root);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static int getSizeOf(String type) throws RuntimeException {
		if (type.equals("int") || type.equals("float") || type.endsWith("*") || type.endsWith(")"))
		{
			return 1;
		}
		else if (type.endsWith("]"))
		{
			int size = Integer.parseInt(type.substring(type.lastIndexOf('[') + 1,type.lastIndexOf(']')));
			return size * getSizeOf(type.substring(0,type.lastIndexOf('[')));
		}
		else
		{
			Integer out = structSize.get(type);
			if (out != null)
			{
				return out.intValue();
			}
			else
			{
				throw new RuntimeException();
			}
		}
	}

	public static void setSizeOf(String type, int size)
	{
		structSize.put(type,size);
	}

	public static void setIndexOf(String type, String member, Integer index)
	{
		structIndex.put(type + "." + member, index);
	}

	public static String getTypeOf(Node node)
	{
		if (!node.getNodeName().equals("value"))
		{
			return node.getAttributes().getNamedItem("type").getTextContent();
		}
		else
		{
			return findVariable(node.getTextContent(),node.getParentNode()).getFirstChild().getTextContent();
		}

	}

	private static Node findVariable(String variable, Node node)
	{
		if (node.getNodeName().equals("program"))
		{
			return findVariable(variable, node.getLastChild());
		}

		for (Node child = node.getLastChild();child != null;child = child.getPreviousSibling())
		{
			if (child.getNodeName().equals("uses") && child.getLastChild().getTextContent().equals(variable))
				return child;
		}

		return findVariable(variable, node.getParentNode());
	}

	public static Node getFunction(String name) {
		NodeList functions = doc.getFirstChild().getFirstChild().getNextSibling().getChildNodes();
		for (int i = 0; i < functions.getLength(); i++)
		{
			if (functions.item(i).getFirstChild().getTextContent().equals(name))
			{
				return functions.item(i);
			}
		}
		return null;
	}
}
