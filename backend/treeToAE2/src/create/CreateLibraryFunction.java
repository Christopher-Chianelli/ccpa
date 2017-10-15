/*
 * backend/treeToAE2/src/create/CreateLibraryFunction.java
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

package create;

import run.TreeToAE2;

public class CreateLibraryFunction {

	public static void defineReadInt()
	{
		System.out.println(".readInt");
		System.out.println("A read next integer");
		System.out.println("N[OUT] ?");
		TreeToAE2.returnToCaller();
	}

	public static void defineReadFloat()
	{
		System.out.println(".readFloat");
		System.out.println("A read next float");
		System.out.println("N[OUT] ?");
		TreeToAE2.returnToCaller();
	}

	public static void definePrintInt()
	{
		System.out.println(".$printInt");
		System.out.println("+");
		System.out.println("L[STACK_TOP]");
		System.out.println("L[ONE]");
		System.out.println("S[STACK_TOP]");
		CreateMemoryOp.getVariableFromStack("0");
		CreateMemoryOp.readFromAddress("DIRTY");
	    System.out.println("+");
	    System.out.println("L[DIRTY]");
	    System.out.println("L[ZERO]");
	    System.out.printf("P\n");
	    System.out.println("-");
		System.out.println("L[STACK_TOP]");
		System.out.println("L[ONE]");
		System.out.println("S[STACK_TOP]");
		TreeToAE2.returnToCaller();
	}

	public static void definePrintNewLine()
	{
		System.out.println(".$printNewLine");
		System.out.println("A write new line");
		TreeToAE2.returnToCaller();
	}

	public static void definePrintFloat()
	{
		System.out.println(".$printFloat");
		System.out.println("+");
		System.out.println("L[STACK_TOP]");
		System.out.println("L[ONE]");
		System.out.println("S[STACK_TOP]");
		CreateMemoryOp.getVariableFromStack("0");
		CreateMemoryOp.readFromAddress("DIRTY");

		CreateMathOp.getExpPart("DIRTY", "EXP0");
		CreateMathOp.getDecPart("DIRTY", "DEC0");

		System.out.println("A set decimal places to 47");
		System.out.println("A write numbers with decimal point");
	    System.out.println("P");
	    System.out.println("A write annotation  * 10^");
	    CreateMathOp.binaryOp("-", "EXP0", "FIFTY", "EXP0");

	    System.out.println("A set decimal places to 0");
	    System.out.println("A write numbers as ##################################################");
	    System.out.println("P");


	    System.out.println("-");
		System.out.println("L[STACK_TOP]");
		System.out.println("L[ONE]");
		System.out.println("S[STACK_TOP]");
		TreeToAE2.returnToCaller();
	}

	public static void definePrintString()
	{
		System.out.println(".$printString");
		System.out.println("+");
		System.out.println("L[STACK_TOP]");
		System.out.println("L[ONE]");
		System.out.println("S[STACK_TOP]");
		CreateMemoryOp.getVariableFromStack("0");
		CreateMemoryOp.readFromAddress("DIRTY");

		for (int i = 0; i < TreeToAE2.myStrings.size(); i++){
			System.out.println("-");
	        System.out.println("L[DIRTY]");
	        System.out.println("L[ONE]");
	        System.out.println("S[DIRTY]");
	        System.out.println("CF?1");
	        System.out.println("CF+25");
	        System.out.printf("A write annotation %s\n",TreeToAE2.myStrings.get(i).substring(1,TreeToAE2.myStrings.get(i).length() - 1));
	        System.out.println("-");
			System.out.println("L[STACK_TOP]");
			System.out.println("L[ONE]");
			System.out.println("S[STACK_TOP]");
			TreeToAE2.returnToCaller();
	    }
	}

	public static void defineReturnFunction() {
		CreateControlOp.createReturnFunction();
	}
}
