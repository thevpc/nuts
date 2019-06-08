/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2019 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.format.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.vpc.app.nuts.NutsElement;
import net.vpc.app.nuts.core.format.elem.DefaultNutsArrayElement;
import net.vpc.app.nuts.core.format.elem.NutsElementFactoryContext;
import net.vpc.app.nuts.core.format.elem.NutsElementUtils;
import net.vpc.app.nuts.core.format.elem.NutsElementFactory;

/**
 *
 * @author vpc
 */
public class NutsElementFactoryJsonElement implements NutsElementFactory {
    
    @Override
    public NutsElement create(Object o, NutsElementFactoryContext context) {
        JsonElement je = (JsonElement) o;
        if (je.isJsonNull()) {
            return NutsElementUtils.NULL;
        } else if (je.isJsonPrimitive()) {
            JsonPrimitive jr = je.getAsJsonPrimitive();
            if (jr.isString()) {
                return NutsElementUtils.forString(jr.getAsString());
            } else if (jr.isNumber()) {
                return NutsElementUtils.forNumber(jr.getAsNumber());
            } else if (jr.isBoolean()) {
                return NutsElementUtils.forBoolean(jr.getAsBoolean());
            } else {
                throw new IllegalArgumentException("Unsupported");
            }
        } else if (je.isJsonArray()) {
            return new DefaultNutsArrayElement(o, context);
        } else if (je.isJsonObject()) {
            return new NutsObjectElementJson(je.getAsJsonObject(), context);
        }
        throw new IllegalArgumentException("Unsupported");
    }
    
}
