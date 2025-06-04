/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.format.xml;

import java.lang.reflect.Type;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import net.thevpc.nuts.elem.NElementMapper;

/**
 *
 * @author thevpc
 */
public class NElementFactoryXmlDocument implements NElementMapper {

    @Override
    public Object destruct(Object o, Type typeOfSrc, NElementFactoryContext context) {
        Document doc = (Document) o;
        Element e = doc.getDocumentElement();
        Object x = NElementFactoryXmlElement.runWithDoc(context,
                () -> context.destruct(e, Element.class),
                doc);
        return x;
    }
    

    @Override
    public NElement createElement(Object o, Type typeOfSrc, NElementFactoryContext context) {
        Document doc = (Document) o;
        Element e = doc.getDocumentElement();
        NElement x = NElementFactoryXmlElement.runWithDoc(context,
                () -> context.createElement(e, Element.class),
                doc);
        return x;
    }

    @Override
    public Object createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        Document doc = XmlUtils.createDocument();
        Node source = NElementFactoryXmlElement.runWithDoc(
                context,
                () -> (Node) context.createObject(o, Element.class),
                doc);
        doc.insertBefore(source,null);
//        doc.adoptNode(source);
        return doc;
    }
    

}
