/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc.deprecated;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 *
 * @author thevpc
 */
public class XmlUtils {

//    public static void main(String[] args) {
//        try {
//            String m = IOUtils.getTextResource(FileSystemTemplater.sourceConvertPath("/pom.xml"));
//            Document doc = load(m);
//            addMavenModule(doc, "toto");
//            System.out.println(toString(doc));
//        } catch (Exception ex) {
//            Logger.getLogger(XmlUtils.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    public static Iterable<Node> toItertable(NodeList list) {
        return new Iterable<Node>() {
            @Override
            public Iterator<Node> iterator() {
                return new NodeListIterator(list);
            }
        };
    }

    public static boolean addMavenModule(Document doc, String moduleName) throws IOException {
        for (Node modules : toItertable(doc.getElementsByTagName("modules"))) {
            if (findChildElement(modules, x -> x.getNodeName().equals(moduleName)) == null) {
                modules.appendChild(createElementWithText(doc, "module", moduleName));
                return true;
            }
        }
        return false;
    }

    public static Element createElementWithText(Document doc, String element, String text) throws IOException {
        Element m = doc.createElement(element);
        m.appendChild(doc.createTextNode(text));
        return m;
    }

    public static void addMavenDependency(Document doc, String groupId, String artifactId, String version, String scope, boolean update) throws IOException {
        for (Node dependencies : toItertable(doc.getElementsByTagName("dependencies"))) {
            Element oldDependency = findChildElement(dependencies, x
                    -> {
                try {
                    return x.getNodeName().equals("dependency")
                            && findChildElementWithText(x, "groupId", groupId) != null
                            && findChildElementWithText(x, "artifactId", artifactId) != null;
                } catch (Exception ex) {
                    return false;
                }
            });

            if (oldDependency != null) {
                Element d = doc.createElement("dependency");
                d.appendChild(createElementWithText(doc, "groupId", groupId));
                d.appendChild(createElementWithText(doc, "artifactId", artifactId));
                d.appendChild(createElementWithText(doc, "version", version));
                if (scope != null) {
                    d.appendChild(createElementWithText(doc, "scope", scope));
                }
                dependencies.appendChild(d);
            } else if (update) {
                Element oldVersion = findChildElementWithText(oldDependency, "version", groupId);
                if (oldVersion == null) {
                    oldDependency.appendChild(createElementWithText(doc, "version", version));
                } else {
                    removeChildren(oldVersion);
                    oldVersion.appendChild(doc.createTextNode(version));
                }
                if (scope != null) {
                    Element oldScope = findChildElementWithText(oldDependency, "scope", groupId);
                    if (oldScope == null) {
                        oldDependency.appendChild(createElementWithText(doc, "scope", scope));
                    } else {
                        removeChildren(oldScope);
                        oldScope.appendChild(doc.createTextNode(scope));
                    }
                }
            }
        }
    }

    public static interface ElementFilter {

        boolean accept(Element e);
    }

    public static interface TextFilter {

        boolean accept(Text e);
    }

    public static Element findChildElementWithText(Node modules, String childName, String childText) throws IOException {
        return findChildElement(modules, x -> {
            try {
                return x.getNodeName().equals(childName) && findChildText(x, y -> {
                    return y.getWholeText().trim().equals(childText);
                }) != null;
            } catch (Exception ex) {
                return false;
            }
        });
    }

    public static <E> ArrayList<E> makeCollection(Iterable<E> iter) {
        ArrayList<E> list = new ArrayList<E>();
        for (E item : iter) {
            list.add(item);
        }
        return list;
    }

    public static void removeChildren(Node modules) throws IOException {
        ArrayList<Node> a = makeCollection(toItertable(modules.getChildNodes()));
        for (Node module : makeCollection(toItertable(modules.getChildNodes()))) {
            modules.removeChild(module);
        }
    }

    public static Element findChildText(Node modules, TextFilter e) throws IOException {
        for (Node module : toItertable(modules.getChildNodes())) {
            if (module instanceof Text && e.accept((Text) module)) {
                return (Element) module;
            }
        }
        return null;
    }

    public static Element findChildElement(Node modules, ElementFilter e) throws IOException {
        for (Node module : toItertable(modules.getChildNodes())) {
            if (module instanceof Element && e.accept((Element) module)) {
                return (Element) module;
            }
        }
        return null;
    }

    public static Document load(String str) throws IOException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(str.getBytes()));

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            return doc;
        } catch (ParserConfigurationException ex) {
            throw new IOException(ex);
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
    }

    public static String toString(Document doc) throws IOException {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StringWriter sw = new StringWriter();
            Result output = new StreamResult(sw);
            Source input = new DOMSource(doc);
            transformer.transform(input, output);
            return sw.toString();
        } catch (TransformerException ex) {
            throw new IOException(ex);
        }
    }

    private static class NodeListIterator implements Iterator<Node> {
        private final NodeList list;
        int i;

        public NodeListIterator(NodeList list) {
            this.list = list;
            i = 0;
        }

        @Override
        public boolean hasNext() {
            return i < list.getLength();
        }

        @Override
        public Node next() {
            Node x = list.item(i);
            i++;
            return x;
        }
    }
}
