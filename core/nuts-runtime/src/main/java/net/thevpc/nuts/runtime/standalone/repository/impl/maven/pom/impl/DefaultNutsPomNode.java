package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DefaultNutsPomNode<T> implements NutsPomNode {
    private Document document;
    private Element element;
    private T object;

    public DefaultNutsPomNode(Element element, T object, Document document) {
        this.element = element;
        this.object = object;
        this.document = document;
    }

    public Element getXmlElement() {
        return element;
    }

    public T getObject() {
        return object;
    }


    protected Element createTextElement(String name, String value) {
        Element groupId = document.createElement(name);
        groupId.setTextContent(value);
        return groupId;
    }

    protected String getTextElement(String name) {
        NodeList nodes = getXmlElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node c = nodes.item(i);
            if (c instanceof Element && ((Element) c).getTagName().equals(name)) {
                return c.getTextContent();
            }
        }
        return null;
    }

    protected void setTextElement(String name, String value, boolean create,boolean removeIfNull) {
        NodeList nodes = getXmlElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node c = nodes.item(i);
            if (c instanceof Element && ((Element) c).getTagName().equals(name)) {
                if(removeIfNull && value==null) {
                    getXmlElement().removeChild(c);
                    return;
                }else {
                    c.setTextContent(value==null?"":value);
                    return;
                }
            }
        }
        if (create) {
            if(removeIfNull && value==null) {
                //ignore
            }else {
                Element child = document.createElement(name);
                child.setTextContent(value);
                getXmlElement().appendChild(child);
            }
        }
    }

    protected void setTextElement(String name, String value, boolean create) {
        NodeList nodes = getXmlElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node c = nodes.item(i);
            if (c instanceof Element && ((Element) c).getTagName().equals(name)) {
                c.setTextContent(value);
                return;
            }
        }
        if (create) {
            Element child = document.createElement(name);
            child.setTextContent(value);
            getXmlElement().appendChild(child);
        }
    }

    public Document getDocument() {
        return document;
    }
}
