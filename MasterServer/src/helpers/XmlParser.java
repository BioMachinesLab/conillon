package helpers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlParser {

	
	public String getMacAddress() {
			
			
			//TODO retirar ciclo
			for (int i = 0; i < nodeList.getLength(); i++) {
	            Node nNode = nodeList.item(i);
	            
	            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	               Element eElement = (Element) nNode;
	               
	               
	               return eElement.getElementsByTagName("mac_address").item(0).getTextContent();
	               
	            }
	         }
			
			return "";
		}
	
	public String getHostName() {
		
		
		//TODO retirar ciclo
		for (int i = 0; i < nodeList.getLength(); i++) {
            Node nNode = nodeList.item(i);
            
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
               Element eElement = (Element) nNode;
               
               
               return eElement.getElementsByTagName("host_name").item(0).getTextContent();
               
            }
         }
		
		return "";
	}
	
	public XmlParser(String _xmlString){
		this.setXmlString(_xmlString);		
		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	
	        ByteArrayInputStream input =  new ByteArrayInputStream(getXmlString().getBytes("UTF-8"));
	        Document doc = dBuilder.parse(input);
	                 
	        doc.getDocumentElement().normalize();
	
	        XPath xPath =  XPathFactory.newInstance().newXPath();
	
	        String expression = "/info";	        
	        
	        nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
	        
		}
		catch (ParserConfigurationException e) {
	         e.printStackTrace();
	      } catch (SAXException e) {
	         e.printStackTrace();
	      } catch (IOException e) {
	         e.printStackTrace();
	      } catch (XPathExpressionException e) {
	         e.printStackTrace();
	      }
	}
	
	private String xmlString;	
	public String getXmlString() { return xmlString; }	
	private void setXmlString(String xmlString) { this.xmlString = xmlString; }
	private NodeList nodeList;
}
