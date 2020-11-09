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
package net.thevpc.nuts.runtime.format.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.runtime.format.elem.NutsElementFactoryContext;
import net.thevpc.nuts.runtime.format.elem.NutsElementFactory;

/**
 *
 * @author vpc
 */
public class NutsElementFactoryJsonElement implements NutsElementFactory {

    @Override
    public NutsElement create(Object o, NutsElementFactoryContext context) {
        JsonElement je = (JsonElement) o;
        if (je.isJsonNull()) {
            return context.builder().forNull();
        } else if (je.isJsonPrimitive()) {
            JsonPrimitive jr = je.getAsJsonPrimitive();
            if (jr.isString()) {
                return context.builder().forString(jr.getAsString());
            } else if (jr.isNumber()) {
                return context.builder().forNumber(jr.getAsNumber());
            } else if (jr.isBoolean()) {
                return context.builder().forBoolean(jr.getAsBoolean());
            } else {
                throw new IllegalArgumentException("Unsupported");
            }
        } else if (je.isJsonArray()) {
            return new NutsArrayElementJson(je.getAsJsonArray(), context);
        } else if (je.isJsonObject()) {
            return new NutsObjectElementJson(je.getAsJsonObject(), context);
        }
        throw new IllegalArgumentException("Unsupported");
    }

}
