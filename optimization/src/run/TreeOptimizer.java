package run;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TreeOptimizer {
	private static Document doc;
	
	private static Document readFromInput() throws Exception
	{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		return docBuilder.parse(System.in);
	}
	
	private static void removeNoOps(Node program)
	{
		NodeList statements = program.getChildNodes();
		for (int i = 0; i < statements.getLength(); i++)
		{
			Node statement = statements.item(i);
			int oldLength = statements.getLength();
			if (statement.getNodeName().equals("op"))
			    removeNoOpsFromOp(program,statement);
			else if (statement.getNodeName().equals("value"))
				program.removeChild(statement);
			if (oldLength > statements.getLength()){
				i = -1;
			}
		}
	}
	
	private static void removeNoOpsFromOp(Node root, Node op)
	{
		if (!op.getNodeName().equals("op"))
			return;
		NamedNodeMap attr = op.getAttributes();
		String operation =  attr.getNamedItem("name").getTextContent();
		if (operation.equals("NO_OP"))
		{
			root.removeChild(op);
		}
		else if (operation.equals(";"))
		{
			NodeList children = op.getChildNodes();
			
			if (children.item(0).getNodeName().equals("op"))
				removeNoOpsFromOp(op,children.item(0));
			else if (children.item(0).getNodeName().equals("value"))
				op.removeChild(children.item(0));
			
			if (children.item(1) == null)
			{
				if (children.item(0).getNodeName().equals("op"))
					removeNoOpsFromOp(op,children.item(0));
				else if (children.item(0).getNodeName().equals("value"))
					op.removeChild(children.item(0));
			}
			else
			{
				if (children.item(1).getNodeName().equals("op"))
					removeNoOpsFromOp(op,children.item(1));
				else if (children.item(1).getNodeName().equals("value"))
					op.removeChild(children.item(1));
			}
			
			if (op.getChildNodes().getLength() == 1)
				root.replaceChild(op.getFirstChild(), op);
			else if (op.getChildNodes().getLength() == 0)
				root.removeChild(op);
		}
		else if (operation.equals("FUN"))
		{
			removeNoOpsFromOp(op,op.getChildNodes().item(1));
		}
	}
	
	private static void removeUnusedVariables(Node program)
	{
		NodeList statements = program.getChildNodes();
		for (int i = 0; i < statements.getLength(); i++)
		{
			Node statement = statements.item(i);
			if (statement.getNodeName().equals("uses"))
			{
			    if (variableNotUsed(program,statement.getChildNodes().item(1)))
			    {
			    	program.removeChild(statement);
			    	i = -1;
			    }
			}
			else if (statement.getNodeName().equals("op"))
			{
				removeUnusedVariables(statement);
			}
		}
	}
	
	private static boolean variableNotUsed(Node root, Node variable)
	{
		if (root.getNodeName().equals("value"))
		{
			if (root.isEqualNode(variable))
				return false;
			else
				return true;
		}
		
		NodeList statements = root.getChildNodes();
		for (int i = 0; i < statements.getLength(); i++)
		{
			Node statement = statements.item(i);
			if (!statement.getNodeName().equals("uses"))
			    if (!variableNotUsed(statement,variable))
			    {
			    	return false;
			    }
		}
		return true;
	}
	
	private static void changeCallOps(Node program)
	{
		NodeList statements = program.getChildNodes();
		for (int i = 0; i < statements.getLength(); i++)
		{
			Node statement = statements.item(i);
			changeCallOpsInStatement(statement, false);
		}
	}
	
	private static void changeCallOpsInStatement(Node statement, boolean isCall)
	{
		if (!statement.getNodeName().equals("op"))
			return;
		NamedNodeMap attr = statement.getAttributes();
		String operation =  attr.getNamedItem("name").getTextContent();
		if (operation.equals("CALL"))
		{
			changeCallOpsInStatement(statement.getLastChild(),true);
		}
		else if (operation.equals(";"))
		{
			if (isCall)
			{
				Attr newOp = doc.createAttribute("name");
				newOp.setValue("COMBINE_ARGS");
				
				attr.setNamedItem(newOp);
				
				changeCallOpsInStatement(statement.getFirstChild(),true);
				changeCallOpsInStatement(statement.getFirstChild().getNextSibling(),true);
			}
			else
			{
				changeCallOpsInStatement(statement.getFirstChild(),false);
				changeCallOpsInStatement(statement.getFirstChild().getNextSibling(),false);
			}
		}
		else if (operation.equals("NO_OP"))
		{
			if (isCall)
			{
				Attr newOp = doc.createAttribute("name");
				newOp.setValue("NO_ARG");
				
				attr.setNamedItem(newOp);
			}
		}
		else
		{
			NodeList children = statement.getChildNodes();
			for (int i = 0; i < children.getLength(); i++)
			{
				changeCallOpsInStatement(children.item(i),isCall);
			}
		}
	}
	
	private static void convertToFinalForm(Node program) throws TransformerException
	{
		Element functions = doc.createElement("functions");
		Element dataStructures = doc.createElement("structures");
		Element init = doc.createElement("init");
		
		removeFunctions(program, functions);
		removeDataStructures(program, dataStructures);
		Node initCode = program.getFirstChild();
		Node globalVariable = initCode.getLastChild();
		
		while (globalVariable.getNodeName().equals("uses"))
		{
			dataStructures.appendChild(globalVariable.cloneNode(true));
			globalVariable = globalVariable.getPreviousSibling();
			initCode.removeChild(globalVariable.getNextSibling());
		}
		
		init.appendChild(initCode.cloneNode(true));
		
		while (program.hasChildNodes())
			program.removeChild(program.getFirstChild());
		
		NodeList myFunctions = functions.getChildNodes();
		
		for (int i = 0; i < myFunctions.getLength();i++)
		{
			for (Node temp = myFunctions.item(i).getLastChild();temp != null;temp = temp.getPreviousSibling())
			{
				if (temp.getLastChild().getTextContent().equals(myFunctions.item(i).getFirstChild().getTextContent()))
				{
					myFunctions.item(i).removeChild(temp);
					break;
				}
			}
		}
		
		Element callMain = doc.createElement("op");
		callMain.setAttribute("name", "CALL");
		callMain.setAttribute("type", "void");
		
		Element mainName = doc.createElement("value");
		mainName.setTextContent("main");
		
		callMain.appendChild(mainName);
		
		Element mainParams = doc.createElement("op");
		mainParams.setAttribute("name", "NO_ARG");
		mainParams.setAttribute("type", "");
		
		callMain.appendChild(mainParams);
		
		init.appendChild(callMain);
		
		Element haltProgram = doc.createElement("op");
		haltProgram.setAttribute("name", "HALT");
		init.appendChild(haltProgram);
		
		program.appendChild(init);
		program.appendChild(functions);
		program.appendChild(dataStructures);
	}
	
	private static boolean removeFunctions(Node node, Element functions) {
		if (node.getNodeName().equals("op") && node.getAttributes().getNamedItem("name").getTextContent().equals("FUN"))
		{
			Element noOp = doc.createElement("op");
			noOp.setAttribute("name", "NO_OP");
			node.getParentNode().replaceChild(noOp, node);
			functions.appendChild(node);
			return true;
		}
		
		boolean out = false;
		Node child = node.getFirstChild();
		while (child != null)
		{
			if (removeFunctions(child,functions))
			{
				child = node.getFirstChild();
				out = true;
			}
			else
			{
			    child = child.getNextSibling();
			}
		}
		return out;
	}
	
	private static boolean removeDataStructures(Node node, Element dataStructures) {
		if (node.getNodeName().equals("data"))
		{
			node.getParentNode().removeChild(node);
			dataStructures.appendChild(node);
			return true;
		}
		
		boolean out = false;
		Node child = node.getFirstChild();
		while (child != null)
		{
			if (removeDataStructures(child,dataStructures))
			{
				child = node.getFirstChild();
				out = true;
			}
			else
			{
			    child = child.getNextSibling();
			}
		}
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		try
		{
		    doc = readFromInput();
		
		    Node program = doc.getFirstChild();
		    Node oldProgram = doc.getFirstChild();
		
		    removeNoOps(program);
		    //removeUnusedVariables(program);
		
		
		    changeCallOps(program);
		    convertToFinalForm(program);
		
		    doc.replaceChild(program, oldProgram);
		    outputFile();
		}
		catch (Exception e)
		{
			System.out.println("<error></error>");
			System.exit(1);
		}
	}
	
	private static void outputFile() throws TransformerException
	{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(System.out);
		transformer.transform(source, result);
	}

}
