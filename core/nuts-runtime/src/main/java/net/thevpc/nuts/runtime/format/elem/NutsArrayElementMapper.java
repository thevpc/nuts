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
package net.thevpc.nuts.runtime.format.elem;

import net.thevpc.nuts.NutsElementType;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsArrayElement;

/**
 *
 * @author vpc
 */
public class NutsArrayElementMapper extends AbstractNutsElement implements NutsArrayElement {

    private final NutsElementFactoryContext context;
    private final List<Object> values = new ArrayList<>();

    public NutsArrayElementMapper(Object array, NutsElementFactoryContext context) {
        super(NutsElementType.ARRAY);
        this.context = context;
        if (array.getClass().isArray()) {
            int count = Array.getLength(array);
            for (int i = 0; i < count; i++) {
                values.add(Array.get(array, i));
            }
        } else if (array instanceof Collection) {
            values.addAll((Collection) array);
        } else if (array instanceof Iterator) {
            Iterator nl = (Iterator) array;
            while (nl.hasNext()) {
                values.add(nl.next());
            }
        } else {
            throw new IllegalArgumentException("Unsupported");
        }
    }

    @Override
    public Collection<NutsElement> children() {
        return values.stream().map(context::toElement).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "[" + children().stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public NutsElement get(int index) {
        return context.toElement(values.get(index));
    }

}
