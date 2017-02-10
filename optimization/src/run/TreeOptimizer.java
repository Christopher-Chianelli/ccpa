package run;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import org.xml.sax.SAXException;

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
	
	public static void main(String[] args) throws Exception {
		Document doc = readFromInput();
		
		Node program = doc.getFirstChild();
		Node oldProgram = doc.getFirstChild();
		
		removeNoOps(program);
		removeUnusedVariables(program);
		
		doc.replaceChild(program, oldProgram);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(System.out);
		transformer.transform(source, result);
	}

}
