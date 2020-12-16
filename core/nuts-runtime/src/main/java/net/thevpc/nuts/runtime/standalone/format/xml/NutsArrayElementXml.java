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
package net.thevpc.nuts.runtime.standalone.format.xml;

import net.thevpc.nuts.runtime.standalone.format.elem.AbstractNutsElement;
import net.thevpc.nuts.runtime.standalone.format.elem.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.w3c.dom.NodeList;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsArrayElement;
import org.w3c.dom.Element;

/**
 *
 * @author thevpc
 */
public class NutsArrayElementXml extends AbstractNutsElement implements NutsArrayElement {

    private NutsElementFactoryContext context;
    private List<Object> values = new ArrayList<>();

    public NutsArrayElementXml(Element array, NutsElementFactoryContext context) {
        super(NutsElementType.ARRAY);
        this.context = context;
        NodeList nl = (NodeList) array;
        int count = nl.getLength();
        for (int i = 0; i < count; i++) {
            values.add(nl.item(i));
        }
    }

    @Override
    public Collection<NutsElement> children() {
        return values.stream().map(x -> context.toElement(x)).collect(Collectors.toList());
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public NutsElement get(int index) {
        return context.toElement(values.get(index));
    }

    @Override
    public String toString() {
        return "[" + children().stream().map(x -> x.toString()).collect(Collectors.joining(", ")) + "]";
    }

}
