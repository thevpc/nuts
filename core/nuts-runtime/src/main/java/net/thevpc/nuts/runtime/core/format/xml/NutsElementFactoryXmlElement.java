/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.core.format.xml;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import net.thevpc.nuts.NutsArrayElementBuilder;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementBuilder;
import net.thevpc.nuts.NutsNamedElement;
import net.thevpc.nuts.NutsObjectElement;
import net.thevpc.nuts.NutsObjectElementBuilder;
import net.thevpc.nuts.runtime.core.format.elem.NutsElementFactory;
import net.thevpc.nuts.runtime.core.format.elem.NutsElementFactoryContext;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author thevpc
 */
public class NutsElementFactoryXmlElement implements NutsElementFactory<Node> {

    public static <V> V runWithDoc(NutsElementFactoryContext context, Supplier<V> impl, Document doc) {
        Stack<Document> docs = (Stack<Document>) context.getProperties().get(Document.class.getName());
        if (docs == null) {
            docs = new Stack<>();
            context.getProperties().put(Document.class.getName(), docs);
            try {
                docs.push(doc != null ? doc : NutsXmlUtils.createDocument(context.getWorkspace().createSession()));
                return impl.get();
            } finally {
                docs.pop();
            }
        } else {
            if (docs.isEmpty() || doc != null) {
                try {
                    docs.push(doc != null ? doc : NutsXmlUtils.createDocument(context.getWorkspace().createSession()));
                    return impl.get();
                } finally {
                    docs.pop();
                }
            }
        }
        return impl.get();
    }

    @Override
    public Node createObject(NutsElement elem, Type typeOfResult, NutsElementFactoryContext context) {
        return runWithDoc(context, () -> createObject0(elem, typeOfResult, context), null);
    }

    protected Node createObject0(NutsElement elem, Type typeOfResult, NutsElementFactoryContext context) {
        if (context.getProperties().get(Document.class.getName()) == null || !(context.getProperties().get(Document.class.getName()) instanceof Stack)) {
            Stack<Document> docs = new Stack<>();
            context.getProperties().put(Document.class.getName(), docs);
            try {
                docs.push(NutsXmlUtils.createDocument(context.getWorkspace().createSession()));
                return createObject(elem, typeOfResult, context);
            } finally {
                docs.pop();
            }
        } else {
            Stack<Document> docs = (Stack<Document>) context.getProperties().get(Document.class.getName());
            if (docs.isEmpty()) {
                try {
                    docs.push(NutsXmlUtils.createDocument(context.getWorkspace().createSession()));
                    return createObject(elem, typeOfResult, context);
                } finally {
                    docs.pop();
                }
            } else {
                //continue;
            }
        }
        Document doc = ((Stack<Document>) context.getProperties().get(Document.class.getName())).peek();
        switch (elem.type()) {
            case NULL: {
                Element e = doc.createElement("null");
                return e;
            }
            case STRING: {
                Element e = doc.createElement("string");
                e.setTextContent(elem.primitive().getString());
                return e;
            }

            case BOOLEAN: {
                return doc.createElement(String.valueOf(elem.primitive().getBoolean()));
            }
            case BYTE: {
                Element e = doc.createElement("byte");
                e.setTextContent(String.valueOf(elem.primitive().getByte()));
                return e;
            }
            case SHORT: {
                Element e = doc.createElement("short");
                e.setTextContent(String.valueOf(elem.primitive().getShort()));
                return e;
            }
            case INTEGER: {
                Element e = doc.createElement("int");
                e.setTextContent(String.valueOf(elem.primitive().getInt()));
                return e;
            }
            case LONG: {
                Element e = doc.createElement("long");
                e.setTextContent(String.valueOf(elem.primitive().getLong()));
                return e;
            }
            case FLOAT: {
                Element e = doc.createElement("float");
                e.setTextContent(String.valueOf(elem.primitive().getFloat()));
                return e;
            }
            case DOUBLE: {
                Element e = doc.createElement("double");
                e.setTextContent(String.valueOf(elem.primitive().getDouble()));
                return e;
            }
            case DATE: {
                Element e = doc.createElement("instant");
                e.setTextContent(elem.primitive().getDate().toString());
                return e;
            }
            case ARRAY: {
                Element e = doc.createElement("array");
                int count = 0;
                for (NutsElement attribute : elem.array().children()) {
                    Node c = createObject(attribute, Element.class, context);
                    if (c != null) {
                        e.appendChild(c);
                        count++;
                    }
                }
                return e;
            }
            case OBJECT: {
                NutsObjectElement obj = normalizeObj(elem.object(), context);
                Element e = doc.createElement("object");
                for (NutsNamedElement ne : obj.children()) {
                    Element elem2 = (Element) createObject(ne.getValue(), NutsElement.class, context);
                    elem2.setAttribute("name", ne.getName());
                }
                return e;
            }
            default: {
                throw new IllegalArgumentException("Unsupported");
            }
        }
    }

    public NutsElement createElement(String type, String value, NutsElementFactoryContext context) {
        switch (type) {
            case "null": {
                return context.getWorkspace().formats().element().elements().forNull();
            }
            case "number": {
                return context.objectToElement(value, Number.class);
            }
            case "boolean": {
                return context.objectToElement(value, Boolean.class);
            }
            case "true": {
                return context.getWorkspace().formats().element().elements().forBoolean(true);
            }
            case "false": {
                return context.getWorkspace().formats().element().elements().forBoolean(true);
            }
            case "byte": {
                return context.objectToElement(value, Byte.class);
            }
            case "short": {
                return context.objectToElement(value, Short.class);
            }
            case "int": {
                return context.objectToElement(value, Integer.class);
            }
            case "long": {
                return context.objectToElement(value, Long.class);
            }
            case "float": {
                return context.objectToElement(value, Float.class);
            }
            case "double": {
                return context.objectToElement(value, Double.class);
            }
            case "char": {
                return context.objectToElement(value, Character.class);
            }
            case "string": {
                return context.objectToElement(value, String.class);
            }
            case "instant": {
                return context.objectToElement(value, Instant.class);
            }
            case "date": {
                return context.objectToElement(value, Date.class);
            }
            case "file": {
                return context.objectToElement(value, File.class);
            }
            case "path": {
                return context.objectToElement(value, Path.class);
            }
            default: {
                throw new IllegalArgumentException("unsupported");
            }
        }
    }

    private NutsObjectElement normalizeObj(NutsObjectElement object, NutsElementFactoryContext context) {
        boolean someUpdates = false;
        Map<String, Object> ok = new LinkedHashMap<>();
        for (NutsNamedElement e : object.children()) {
            Object o = ok.get(e.getName());
            if (o == null) {
                ok.put(e.getName(), e.getValue());
            } else {
                if (o instanceof NutsElement) {
                    List<Object> li = new ArrayList<>();
                    li.add(o);
                    li.add(e.getValue());
                    ok.put(e.getName(), li);
                } else {
                    List<Object> li = (List<Object>) o;
                    li.add(e.getValue());
                    someUpdates = true;
                }
            }
        }
        if (!someUpdates) {
            return object;
        }
        NutsObjectElementBuilder o2 = context.getWorkspace().formats().element().elements().forObject();
        for (Map.Entry<String, Object> entry : ok.entrySet()) {
            o2.set(entry.getKey(), context.objectToElement(entry.getValue(), null));
        }
        return o2.build();
    }

    private static class NodeInfo {

        String type;
        String name;
        String value;

        public NodeInfo(Element e) {
            String name0 = e.getAttribute("name");
            name = name0 == null ? e.getTagName() : name0;
            type = e.getAttribute("type") != null ? e.getAttribute("type") : name0 != null ? e.getTagName() : "string";
            value = e.getAttribute("value");
        }

    }

    private String resolveValue(Element e) {
        String value1 = e.getAttribute("value");
        String value2 = e.getTextContent();
        if (value2 == null) {
            return value1;
        }
        if (value1 == null) {
            return value2;
        }
        return value1 + value2;
    }

    @Override
    public NutsElement createElement(Node node, Type typeOfSrc, NutsElementFactoryContext context) {
        NutsElementBuilder elements = context.getWorkspace().formats().element().elements();
        if (node instanceof Attr) {
            Attr at = (Attr) node;
            return elements.forObject().set(at.getName(), context.objectToElement(at.getValue(), String.class)).build();
        }
        if (node instanceof CDATASection) {
            CDATASection d = (CDATASection) node;
            return elements.forString(d.getWholeText());
        }
        if (node instanceof Text) {
            Text d = (Text) node;
            return elements.forString(d.getWholeText());
        }
        Element element = (Element) node;
        NodeInfo ni = new NodeInfo(element);
        switch (ni.type) {
            case "object": {
                NutsObjectElementBuilder obj = elements.forObject();
                NamedNodeMap attrs = element.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Attr n = (Attr) attrs.item(i);
                    obj.set(n.getName(), context.objectToElement(n.getValue(), null));
                }
                NodeList children = element.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node n = (Node) children.item(i);
                    if (n instanceof Element) {
                        Element e = (Element) n;
                        NodeInfo ni2 = new NodeInfo(e);
                        obj.set(ni2.name, createElement(ni2.type, ni2.value, context));
                    } else if (n instanceof Text) {
                        NutsElement e = createElement(n, Text.class, context);
                        obj.set("content", e);
                    }
                }
                return obj.build();
            }
            case "array": {
                NutsArrayElementBuilder obj = elements.forArray();
                NodeList attrs = element.getChildNodes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node n = (Node) attrs.item(i);
                    obj.add(createElement(n, typeOfSrc, context));
                }
                return obj.build();
            }
            case "boolean": {
                return context.objectToElement(resolveValue(element), Boolean.class);
            }
            case "byte": {
                return context.objectToElement(resolveValue(element), Byte.class);
            }
            case "short": {
                return context.objectToElement(resolveValue(element), Short.class);
            }
            case "int": {
                return context.objectToElement(resolveValue(element), Integer.class);
            }
            case "long": {
                return context.objectToElement(resolveValue(element), Long.class);
            }
            case "float": {
                return context.objectToElement(resolveValue(element), Float.class);
            }
            case "double": {
                return context.objectToElement(resolveValue(element), Double.class);
            }
            case "char": {
                return context.objectToElement(resolveValue(element), Character.class);
            }
            case "string": {
                return context.objectToElement(resolveValue(element), String.class);
            }
            case "instant": {
                return context.objectToElement(resolveValue(element), Instant.class);
            }
            case "date": {
                return context.objectToElement(resolveValue(element), Date.class);
            }
            case "file": {
                return context.objectToElement(resolveValue(element), File.class);
            }
            case "path": {
                return context.objectToElement(resolveValue(element), Path.class);
            }
            default: {
                throw new IllegalArgumentException("unsupported");
            }
        }
    }

}
