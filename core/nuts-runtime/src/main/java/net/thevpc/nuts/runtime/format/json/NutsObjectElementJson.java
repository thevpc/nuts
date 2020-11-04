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
package net.thevpc.nuts.runtime.format.json;

import net.thevpc.nuts.NutsElementType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsNamedElement;
import net.thevpc.nuts.runtime.format.elem.DefaultNutsNamedElement;
import net.thevpc.nuts.runtime.format.elem.NutsElementFactoryContext;
import net.thevpc.nuts.runtime.format.elem.NutsObjectElementBase;

/**
 *
 * @author vpc
 */
class NutsObjectElementJson extends NutsObjectElementBase {

    private final JsonObject value;

    public NutsObjectElementJson(JsonObject value, NutsElementFactoryContext context) {
        super(context);
        this.value = value;
    }

    @Override
    public NutsElementType type() {
        return NutsElementType.OBJECT;
    }

    @Override
    public Collection<NutsNamedElement> children() {
        List<NutsNamedElement> all = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : value.entrySet()) {
            all.add(new DefaultNutsNamedElement(entry.getKey(), context.toElement(entry.getValue())));
        }
        return all;
    }

    @Override
    public NutsElement get(String name) {
        JsonElement o = value.get(name);
        if (o == null) {
            return null;
        }
        return context.toElement(o);
    }

    @Override
    public int size() {
        return value.size();
    }

}
