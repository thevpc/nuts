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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.format.xml;

import net.vpc.app.nuts.runtime.format.elem.*;
import net.vpc.app.nuts.NutsElementType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.w3c.dom.NodeList;
import net.vpc.app.nuts.NutsElement;
import net.vpc.app.nuts.NutsArrayElement;
import org.w3c.dom.Element;

/**
 *
 * @author vpc
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
