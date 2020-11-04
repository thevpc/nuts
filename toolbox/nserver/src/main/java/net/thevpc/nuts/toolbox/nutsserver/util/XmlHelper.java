package net.thevpc.nuts.toolbox.nutsserver.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.util.Stack;

public class XmlHelper {
    private Document doc;
    private Stack<Element> stack = new Stack<>();
    private Element last;

    public XmlHelper() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        this.doc = docBuilder.newDocument();
    }

    public XmlHelper(Document doc) {
        this.doc = doc;
    }

    public XmlHelper pop() {
        last = stack.pop();
        return this;
    }

    public XmlHelper push(String name) {
        Element node = doc.createElement(name);
        if (!stack.isEmpty()) {
            Element t = stack.peek();
            t.appendChild(node);
        }else{
            doc.appendChild(node);
        }
        stack.push(node);
        last = node;
        return this;
    }

    public XmlHelper append(String name, String value) {
        Element node = doc.createElement(name);
        node.setTextContent(value);
        Element t = stack.peek();
        t.appendChild(node);
        last = node;
        return this;
    }

    public Document getDocument() {
        return doc;
    }

    public byte[] toXmlBytes() throws TransformerException {
        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(this.getDocument());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(outputStream);
        transformer.transform(source, result);
        return outputStream.toByteArray();
    }
}
