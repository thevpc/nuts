/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.format.xml;

import net.thevpc.nuts.NutsElementType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.thevpc.nuts.NutsElement;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import net.thevpc.nuts.NutsNamedElement;
import net.thevpc.nuts.NutsXmlFormat;
import net.thevpc.nuts.runtime.format.elem.DefaultNutsNamedElement;
import net.thevpc.nuts.runtime.format.elem.NutsElementFactoryContext;
import net.thevpc.nuts.runtime.format.elem.NutsObjectElementBase;
import net.thevpc.nuts.runtime.util.common.CoreCommonUtils;

/**
 *
 * @author vpc
 */
public class NutsObjectElementXml extends NutsObjectElementBase {

    private Element value;

    public NutsObjectElementXml(Element value, NutsElementFactoryContext context) {
        super(context);
        this.value = value;
    }

    @Override
    public NutsElementType type() {
        return NutsElementType.OBJECT;
    }

    private NutsXmlFormat getNutsElementXmlConverter() {
        NutsXmlFormat xml = (NutsXmlFormat) context.getProperties().get(DefaultNutsXmlFormat.class.getName());
        if (xml != null) {
            return xml;
        }
        return context.getWorkspace().formats().xml();
    }

    @Override
    public Collection<NutsNamedElement> children() {
        List<NutsNamedElement> all = new ArrayList<>();
        DefaultNutsXmlFormat xml = (DefaultNutsXmlFormat) getNutsElementXmlConverter();
        NamedNodeMap aa = value.getAttributes();
        NutsElementType type = null;
        for (int i = 0; i < aa.getLength(); i++) {
            Node object = aa.item(i);
            String name = object.getNodeName();
            String value = object.getNodeValue();
            if (name.equals(xml.getTypeAttributeName())) {
                type = CoreCommonUtils.parseEnumString(value, NutsElementType.class, true);
            } else {
                all.add(new DefaultNutsNamedElement(xml.getAttributePrefix() + name, context.toElement(value)));
            }
        }
        NodeList cn = value.getChildNodes();
        for (int i = 0; i < cn.getLength(); i++) {
            Node object = cn.item(i);
            if (object instanceof Element) {
                all.add(new DefaultNutsNamedElement(object.getNodeName(), context.toElement(object)));
            }
        }
        return all;
    }

    @Override
    public NutsElement get(String name0) {
        DefaultNutsXmlFormat xml = (DefaultNutsXmlFormat) getNutsElementXmlConverter();
        NamedNodeMap aa = value.getAttributes();
        NutsElementType type = null;
        for (int i = 0; i < aa.getLength(); i++) {
            Node object = aa.item(i);
            String name = object.getNodeName();
            String value = object.getNodeValue();
            if (name.equals(xml.getTypeAttributeName())) {
                type = CoreCommonUtils.parseEnumString(value, NutsElementType.class, true);
            } else {
                if ((xml.getAttributePrefix() + name).equals(name0)) {
                    return context.toElement(value);
                }
            }
        }
        NodeList cn = value.getChildNodes();
        for (int i = 0; i < cn.getLength(); i++) {
            Node object = cn.item(i);
            if (object instanceof Element) {
                if (object.getNodeName().equals(name0)) {
                    return context.toElement(object);
                }
            }
        }
        return null;
    }

    @Override
    public int size() {
        DefaultNutsXmlFormat xml = (DefaultNutsXmlFormat) getNutsElementXmlConverter();
        NamedNodeMap aa = value.getAttributes();
        int size = 0;
        for (int i = 0; i < aa.getLength(); i++) {
            Node object = aa.item(i);
            String name = object.getNodeName();
            if (name.equals(xml.getTypeAttributeName())) {
                //
            } else {
                size++;
            }
        }
        NodeList cn = value.getChildNodes();
        for (int i = 0; i < cn.getLength(); i++) {
            Node object = cn.item(i);
            if (object instanceof Element) {
                size++;
            }
        }
        return size;
    }

}
