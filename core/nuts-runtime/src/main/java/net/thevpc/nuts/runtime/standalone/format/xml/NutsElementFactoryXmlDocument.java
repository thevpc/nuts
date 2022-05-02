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
package net.thevpc.nuts.runtime.standalone.format.xml;

import java.lang.reflect.Type;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElementFactoryContext;
import net.thevpc.nuts.runtime.standalone.util.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import net.thevpc.nuts.elem.NutsElementMapper;

/**
 *
 * @author thevpc
 */
public class NutsElementFactoryXmlDocument implements NutsElementMapper {

    @Override
    public Object destruct(Object o, Type typeOfSrc, NutsElementFactoryContext context) {
        Document doc = (Document) o;
        Element e = doc.getDocumentElement();
        Object x = NutsElementFactoryXmlElement.runWithDoc(context,
                () -> context.destruct(e, Element.class),
                doc);
        return x;
    }
    

    @Override
    public NutsElement createElement(Object o, Type typeOfSrc, NutsElementFactoryContext context) {
        Document doc = (Document) o;
        Element e = doc.getDocumentElement();
        NutsElement x = NutsElementFactoryXmlElement.runWithDoc(context,
                () -> context.objectToElement(e, Element.class),
                doc);
        return x;
    }

    @Override
    public Object createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        Document doc = XmlUtils.createDocument(context.getSession());
        Node source = NutsElementFactoryXmlElement.runWithDoc(
                context,
                () -> (Node) context.elementToObject(o, Element.class),
                doc);
        doc.insertBefore(source,null);
//        doc.adoptNode(source);
        return doc;
    }
    

}
