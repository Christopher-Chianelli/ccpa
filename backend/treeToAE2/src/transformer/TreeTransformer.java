/*
 * backend/treeToAE2/src/transformer/TreeTransformer.java
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

package transformer;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import run.TreeToAE2;

public class TreeTransformer {
	private static Document doc = TreeToAE2.doc;
	private static int numOfUniqueNames = 0;
	private static final int GLOBAL_VARIABLES_START = 969;
	public static int numOfTempRegisters = 0;

	private static String getUniqueName()
	{
		String out = "$F" + numOfUniqueNames;
		numOfUniqueNames++;
		return out;
	}

	public static void giveFunctionsGlobalAddresses(Node root)
	{
		NodeList functions = root.getFirstChild().getNextSibling().getChildNodes();
		Node globalData = root.getLastChild();
		for (int i = 0; i < functions.getLength(); i++)
		{
			HashSet<String> function = new HashSet<String>();
			function.add(functions.item(i).getFirstChild().getTextContent());
			if (!isFunctionRecursive(functions.item(i),function))
			{
				Element element = (Element) functions.item(i);
				element.setAttribute("isRecursive", "false");
				HashSet<String> localVariables = new HashSet<String>();
				changeFunctionVariablesToGlobal(functions.item(i).getFirstChild().getTextContent(),
						functions.item(i),
						globalData,
						localVariables);
			}
			else
			{
				Element element = (Element) functions.item(i);
				element.setAttribute("isRecursive", "true");
			}
		}
	}

	private static void changeFunctionVariablesToGlobal(String name, Node node, Node globalData, HashSet<String> localVariables)
	{
		if (node.getNodeName().equals("uses"))
			return;

		if (node.getNodeName().equals("value"))
		{
			if (localVariables.contains(node.getTextContent()))
			{
				node.setTextContent(name + ":" + node.getTextContent());
			}
		}

		if (node.getNodeName().equals("op"))
		{
			NamedNodeMap attr = node.getAttributes();
			NodeList children = node.getChildNodes();
			String operation = attr.getNamedItem("name").getTextContent();

			if (!operation.equals("STRING") && !operation.equals("FLOAT") && !operation.equals("INT"))
			{
				if (operation.equals("GET_MEMBER") || operation.equals("GET_ACCESS"))
				{
					changeFunctionVariablesToGlobal(name, children.item(1),globalData,localVariables);
				}
				else
				{
					for (int i = children.getLength() - 1; i >= 0; i--)
					{
						if (children.item(i).getNodeName().equals("uses"))
						{
							if (!localVariables.contains(children.item(i).getLastChild().getTextContent()))
							{
								localVariables.add(children.item(i).getLastChild().getTextContent());

								Node globalVariable = TreeToAE2.doc.createElement("uses");
								Node variableType = children.item(i).getFirstChild().cloneNode(true);
								Node variableName = children.item(i).getLastChild().cloneNode(true);

								variableName.setTextContent(name + ":" + children.item(i).getLastChild().getTextContent());

								globalVariable.appendChild(variableType);
								globalVariable.appendChild(variableName);
								globalData.appendChild(globalVariable);
							}

						}
						changeFunctionVariablesToGlobal(name,children.item(i),globalData,localVariables);
					}
				}
			}
		}

	}

	private static boolean isFunctionRecursive(Node node, HashSet<String> names)
	{
		if (!node.getNodeName().equals("op"))
			return false;

		NamedNodeMap attr = node.getAttributes();
		String operation = attr.getNamedItem("name").getTextContent();
		NodeList children = node.getChildNodes();

		if (operation.equals("CALL"))
		{
			if (names.contains(node.getFirstChild().getTextContent()))
			{
				return true;
			}
			else
			{
				HashSet<String> temp = new HashSet<String>(names);
				temp.add(node.getFirstChild().getTextContent());
				Node calledFunction = TreeToAE2.getFunction(node.getFirstChild().getTextContent());
				boolean out = (calledFunction == null)? false : isFunctionRecursive(calledFunction, temp);

				for (int i = 1; i < children.getLength(); i++)
				{
					out |= isFunctionRecursive(children.item(i), temp);
				}
				return out;
			}
		}
		else
		{
			boolean out = false;
			for (int i = 0; i < children.getLength(); i++)
			{
				out |= isFunctionRecursive(children.item(i), names);
			}
			return out;
		}
	}

	public static void moveFunctionCalls(Node root)
	{
		NodeList init = root.getFirstChild().getChildNodes();
		NodeList functions = root.getFirstChild().getNextSibling().getChildNodes();

		for (int i = 0; i < init.getLength(); i++)
		{
			moveFunctionCallsInNode(init.item(i));
		}

		for (int i = 0; i < functions.getLength(); i++)
		{
			moveFunctionCallsInNode(functions.item(i));
		}

	}

	public static void moveFunctionCallsInNode(Node node)
	{
		if (!node.getNodeName().equals("op"))
			return;

		NamedNodeMap attr = node.getAttributes();
		String operation = attr.getNamedItem("name").getTextContent();
		NodeList children = node.getChildNodes();

		for (int i = 0; i < children.getLength(); i++)
		{
			moveFunctionCallsInNode(children.item(i));
		}

		if (operation.equals("CALL"))
		{
			Node parent = node.getParentNode();
			Node temp = node;

			if (parent != null && parent.getAttributes().getNamedItem("name") != null){
				String parentOp = node.getParentNode().getAttributes().getNamedItem("name").getTextContent();
			while (!parentOp.equals(";") && !parentOp.equals("while") && !parentOp.equals("doWhile") &&  !parentOp.equals("for") && !parentOp.equals("if") && !parentOp.equals("if/else") && !parentOp.equals("ASSIGN"))
			{
				temp = parent;
				parent = temp.getParentNode();
				if (parent == null || parent.getAttributes().getNamedItem("name") == null)
					return;
				parentOp = parent.getAttributes().getNamedItem("name").getTextContent();
			}

			if (node.getParentNode() != parent)
			{
				Element assignOp = doc.createElement("op");
				Element joinOps = doc.createElement("op");
				Element funName = doc.createElement("value");
				Element funType = doc.createElement("value");
				Element usesVar = doc.createElement("uses");

				String type;
				String name = getUniqueName();

				type =  attr.getNamedItem("type").getTextContent();

				funName.setTextContent(name);
				funType.setTextContent(type);

				usesVar.appendChild(funType.cloneNode(true));
				usesVar.appendChild(funName.cloneNode(true));

				assignOp.setAttribute("name", "ASSIGN");
				assignOp.setAttribute("type", type);
				assignOp.appendChild(node.cloneNode(true));
				assignOp.appendChild(funName.cloneNode(true));

				node.getParentNode().replaceChild(funName.cloneNode(true), node);

				joinOps.setAttribute("name", ";");
				joinOps.setAttribute("type", "");
				joinOps.appendChild(temp.cloneNode(true));
				joinOps.appendChild(assignOp.cloneNode(true));

				parent.replaceChild(joinOps.cloneNode(true), temp);
				parent.appendChild(usesVar);
			}
			}
		}
	}

	public static void convertPrintf(Node root)
	{
		NodeList init = root.getFirstChild().getChildNodes();
		NodeList functions = root.getFirstChild().getNextSibling().getChildNodes();

		for (int i = 0; i < init.getLength(); i++)
		{
			convertPrintfInNode(init.item(i));
		}

		for (int i = 0; i < functions.getLength(); i++)
		{
			convertPrintfInNode(functions.item(i));
		}

	}

	public static void convertPrintfInNode(Node node)
	{
		if (!node.getNodeName().equals("op"))
			return;

		NamedNodeMap attr = node.getAttributes();
		String operation = attr.getNamedItem("name").getTextContent();
		NodeList children = node.getChildNodes();

		if (operation.equals("CALL") && node.getFirstChild().getTextContent().equals("printf"))
		{
			Node lastArg = node.getFirstChild().getNextSibling();
			NamedNodeMap argAttr = lastArg.getAttributes();
			String argType = argAttr.getNamedItem("name").getTextContent();
			LinkedList<Node> args = new LinkedList<Node>();
			while (argType.equals("COMBINE_ARGS"))
			{
				args.addFirst(lastArg.getFirstChild());
				lastArg = lastArg.getFirstChild().getNextSibling();
				argAttr = lastArg.getAttributes();
				argType = argAttr.getNamedItem("name").getTextContent();
			}
			String format = lastArg.getFirstChild().getTextContent();
			format = format.substring(1,format.length() - 1).replace("\\n", "%n");
			Pattern regex = Pattern.compile("%(s|d|f|n)");
			Matcher m = regex.matcher(format);
			Element toReplaceNodeWith = doc.createElement("op");
			toReplaceNodeWith.setAttribute("name", "NO_OP");

			int oldStart = 0;
			while (m.find())
			{
				Element temp = doc.createElement("op");
				temp.setAttribute("name", ";");
				Element firstChild = doc.createElement("op");
				Element toCall = doc.createElement("value");

				firstChild.setAttribute("name","CALL");
				if (m.group(1).equals("n"))
				{
					 toCall.setTextContent("$printNewLine");
					 Element temp2 = doc.createElement("op");
					 temp2.setAttribute("name", "NO_ARG");
					 args.addFirst(temp2);
			    }
				else if (m.group(1).equals("s"))
				{
					 toCall.setTextContent("$printString");
				}
				else if (m.group(1).equals("d"))
				{
					toCall.setTextContent("$printInt");
				}
				else if (m.group(1).equals("f"))
				{
					toCall.setTextContent("$printFloat");
				}
				firstChild.appendChild(toCall);
				firstChild.appendChild(args.removeFirst());

				//Also print the non-formatted part!
				Element temp2 = doc.createElement("op");
				temp2.setAttribute("name", ";");
				Element stringPart = doc.createElement("op");
				stringPart.setAttribute("name","CALL");
				Element callString = doc.createElement("value");
				callString.setTextContent("$printString");
				Element toCallArg = doc.createElement("op");
				toCallArg.setAttribute("name", "STRING");
				Element toCallArgString = doc.createElement("value");
				toCallArgString.setTextContent("\"" + format.substring(oldStart, m.start()) + "\"");


				toCallArg.appendChild(toCallArgString);
				stringPart.appendChild(callString);
				stringPart.appendChild(toCallArg);

				temp2.appendChild(firstChild);
				temp2.appendChild(stringPart);

				temp.appendChild(temp2);
				temp.appendChild(toReplaceNodeWith);

				toReplaceNodeWith = temp;

				oldStart = m.end();
			}

			Element temp2 = doc.createElement("op");
			temp2.setAttribute("name", ";");
			Element stringPart = doc.createElement("op");
			stringPart.setAttribute("name","CALL");
			Element callString = doc.createElement("value");
			callString.setTextContent("$printString");
			Element toCallArg = doc.createElement("op");
			toCallArg.setAttribute("name", "STRING");
			Element toCallArgString = doc.createElement("value");
			toCallArgString.setTextContent("\"" + format.substring(oldStart) + "\"");


			toCallArg.appendChild(toCallArgString);
			stringPart.appendChild(callString);
			stringPart.appendChild(toCallArg);

			temp2.appendChild(toReplaceNodeWith);
			temp2.appendChild(stringPart);

			toReplaceNodeWith = temp2;

			node.getParentNode().replaceChild(toReplaceNodeWith, node);
		}
		else
		{
			for (int i = 0; i < children.getLength(); i++)
			{
				convertPrintfInNode(children.item(i));
			}
		}
	}

	public static void giveVariablesAddresses(Node root)
	{
		NodeList data = root.getLastChild().getChildNodes();
		NodeList functions = root.getFirstChild().getNextSibling().getChildNodes();

		int index = 0;
		for (int i = 0; i < functions.getLength(); i++)
		{
			int varCount = giveVariablesAddressesInNode(functions.item(i),0);
			Element element = (Element) functions.item(i);
			element.setAttribute("stackSize", Integer.toString(varCount));
		}

		index = GLOBAL_VARIABLES_START - numOfTempRegisters;
		for (int i = 0; i < data.getLength(); i++)
		{
			if (data.item(i).getNodeName().equals("uses"))
			{
				Element element = (Element) data.item(i);
				index -= TreeToAE2.getSizeOf(element.getFirstChild().getTextContent());
				element.setAttribute("address", "G" + index);
			}
		}
	}

	private static int giveVariablesAddressesInNode(Node node, int index) {
		for (Node child = node.getFirstChild();child != null;child = child.getNextSibling())
		{
			if (child.getNodeName().equals("uses"))
			{
				Element element = (Element) child;
				element.setAttribute("address", "V" + index);
				try {
					index += TreeToAE2.getSizeOf(child.getFirstChild().getTextContent());
				}  catch (Exception e) {
					e.printStackTrace();
				}
			}
			else
			{
				index = giveVariablesAddressesInNode(child,index);
			}
		}
		return index;
	}

	public static void getMaxRequiredRegisters(Node root)
	{
		int max = getMaxRequiredRegistersInNode(root.getFirstChild());
		NodeList functions = root.getFirstChild().getNextSibling().getChildNodes();
		for (int i = 0; i < functions.getLength(); i++)
		{
			max = Math.max(max, getMaxRequiredRegistersInNode(functions.item(i)));
		}
		numOfTempRegisters =  max;
	}

	private static int getMaxRequiredRegistersInNode(Node node)
	{
		int max = 0;
		if (!node.getNodeName().equals("op"))
			return 0;

		NamedNodeMap attr = node.getAttributes();
		String operation = attr.getNamedItem("name").getTextContent();
		NodeList children = node.getChildNodes();

		if (operation.equals(";") || operation.equals("if") || operation.equals("while") || operation.equals("doWhile"))
		{
			return Math.max(getMaxRequiredRegistersInNode(children.item(0)),
					        getMaxRequiredRegistersInNode(children.item(1)));
		}
		else if (operation.equals("if/else") || operation.equals("for"))
		{
			return Math.max(Math.max(getMaxRequiredRegistersInNode(children.item(0)),
					        getMaxRequiredRegistersInNode(children.item(1))),
					        getMaxRequiredRegistersInNode(children.item(2)));
		}
		else
		{
			for (int i = 0; i < children.getLength(); i++)
			{
				max = Math.max(max, getMaxRequiredRegistersInNode(children.item(i)));
			}
			return max + 1;
		}
	}

	public static void setStructSizes(Node program)
	{
		Node structures = program.getLastChild();
		LinkedList<Node> structQueue = new LinkedList<Node>();
		LinkedList<Node> unionQueue = new LinkedList<Node>();
		LinkedList<Node> enumQueue = new LinkedList<Node>();
		for (Node n = structures.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeName().equals("data"))
			{
				String kind = n.getAttributes().getNamedItem("kind").getTextContent();
				if (kind.equals("STRUCT")){
			        structQueue.addLast(n);
			    }
				else if (kind.equals("UNION")){
					unionQueue.addLast(n);
			    }
				else if (kind.equals("ENUM")){
					enumQueue.addLast(n);
				}
			}
		}

		Node last = null;
		while (!structQueue.isEmpty())
		{
			Node current = structQueue.pop();
			Node node = current;
			int size = 0;
			boolean breaked = false;

			for (Node n = node.getLastChild(); !n.getNodeName().equals("value"); n = n.getPreviousSibling())
			{
				try
				{
				    int memberSize = TreeToAE2.getSizeOf(n.getFirstChild().getTextContent());
				    size += memberSize;
				}
				catch (Exception e)
				{
					if (current != last)
					{
						if (last == null)
						    last = current;
						structQueue.addLast(current);
						breaked = true;
						break;
					}
					else
					{
						System.err.printf("%s has undefined size.\n", n.getFirstChild().getTextContent());
						System.err.println("Infinite Descent Structure Detected. Terminating Complication.");
						System.exit(1);
					}
				}
			}

			if (!breaked)
			{
				TreeToAE2.setSizeOf("struct " + node.getFirstChild().getTextContent(), size);
				last = null;
			}
		}

		while (!unionQueue.isEmpty())
		{
			Node current = unionQueue.pop();
			Node node = current;
			int size = 0;
			boolean breaked = false;

			for (Node n = node.getLastChild(); !n.getNodeName().equals("value"); n = n.getPreviousSibling())
			{
				try
				{
				    int memberSize = TreeToAE2.getSizeOf(n.getFirstChild().getTextContent());
				    if (memberSize > size)
				        size = memberSize;
				}
				catch (Exception e)
				{
					if (current != last)
					{
						if (last == null)
						    last = current;
						unionQueue.addLast(current);
						breaked = true;
						break;
					}
					else
					{
						System.err.printf("%s has undefined size.\n", n.getFirstChild().getTextContent());
						System.err.println("Infinite Descent Union Detected. Terminating Complication.");
						System.exit(1);
					}
				}
			}

			if (!breaked)
			{
				TreeToAE2.setSizeOf("union " + node.getFirstChild().getTextContent(), size);
				last = null;
			}
		}

		while (!enumQueue.isEmpty())
		{
			Node current = enumQueue.pop();
			Node node = current;
			TreeToAE2.setSizeOf("union " + node.getFirstChild().getTextContent(), 1);
			last = null;
		}
	}

	public static void setStructIndices(Node program)
	{
		Node structures = program.getLastChild();
		for (Node n = structures.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeName().equals("data"))
			{
				String kind = n.getAttributes().getNamedItem("kind").getTextContent();
				if (kind.equals("STRUCT"))
				{
					int totalSize = 0;
					for (Node child = n.getLastChild(); !child.getNodeName().equals("value"); child = child.getPreviousSibling())
					{
						TreeToAE2.setIndexOf("struct " + n.getFirstChild().getTextContent(), child.getLastChild().getTextContent(), totalSize);
						try
						{
						    totalSize += TreeToAE2.getSizeOf(child.getFirstChild().getTextContent());
						}
						catch(Exception e)
						{
						}
					}
				}
				else if (kind.equals("UNION"))
				{
					for (Node child = n.getLastChild(); !child.getNodeName().equals("value"); child = child.getPreviousSibling())
					{
						TreeToAE2.setIndexOf("union " + n.getFirstChild().getTextContent(), child.getLastChild().getTextContent(), 0);
					}
				}
				else if (kind.equals("ENUM"))
				{
					int i = 0;
					for (Node child = n.getLastChild(); !child.getNodeName().equals("value"); child = child.getPreviousSibling())
					{
						TreeToAE2.setIndexOf("union " + n.getFirstChild().getTextContent(), child.getLastChild().getTextContent(), i);
						i++;
					}
				}
			}
		}
	}

	public static void printDoc() throws TransformerException
	{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(System.err);
		transformer.transform(source, result);
	}
}
