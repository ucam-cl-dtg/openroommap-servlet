package ojw28.orm.utils;

import java.io.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlHelper {
	
	public static Document createDocument(String iName) throws ParserConfigurationException
	{
		DocumentBuilderFactory lFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder lBuilder = lFactory.newDocumentBuilder();
		DOMImplementation lBuilderImpl = lBuilder.getDOMImplementation();
		return lBuilderImpl.createDocument(null, iName, null);
	}
	
	public static void writeDocument(OutputStream iStream, Element iElement) throws TransformerException
	{
		TransformerFactory lTransformFactory = TransformerFactory.newInstance();
		Transformer lTransformer = lTransformFactory.newTransformer();
		
		DOMSource lSource = new DOMSource(iElement);
		StreamResult lStreamResult = new StreamResult(iStream);
		lTransformer.transform(lSource, lStreamResult); 
	}
	
	public static Document parse(InputSource iSource) throws SAXException, IOException, ParserConfigurationException
	{
		DocumentBuilder builder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();  
		return builder.parse(iSource);
	}
	
	public static Document parse(File iFile) throws SAXException, IOException, ParserConfigurationException
	{
		DocumentBuilder builder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();  
		return builder.parse(iFile);
	}

}
