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
package net.thevpc.nuts.runtime.optional.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.thevpc.nuts.NutsArrayElementBuilder;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsObjectElementBuilder;
import net.thevpc.nuts.NutsPrimitiveElement;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.elem.NutsElementFactoryContext;
import net.thevpc.nuts.runtime.core.format.elem.NutsElementStreamFormat;
import net.thevpc.nuts.NutsElementEntry;
import net.thevpc.nuts.NutsElementType;
import net.thevpc.nuts.NutsObjectElement;

/**
 *
 * @author vpc
 */
public class GsonItemSerializeManager implements NutsElementStreamFormat {

    private Gson GSON_COMPACT;
    private Gson GSON_PRETTY;
//    private NutsElementFactoryContext dummyContext;
    protected NutsElementFactoryContext context;

    public GsonItemSerializeManager(NutsElementFactoryContext context) {
        this.context = context;
    }

    public NutsElement gsonToElement(JsonElement o) {
        JsonElement je = (JsonElement) o;
        NutsWorkspace ws = context.getWorkspace();
        if (je.isJsonNull()) {
            return ws.formats().element().elements().forNull();
        } else if (je.isJsonPrimitive()) {
            JsonPrimitive jr = je.getAsJsonPrimitive();
            if (jr.isString()) {
                return ws.formats().element().elements().forString(jr.getAsString());
            } else if (jr.isNumber()) {
                return ws.formats().element().elements().forNumber(jr.getAsNumber());
            } else if (jr.isBoolean()) {
                return ws.formats().element().elements().forBoolean(jr.getAsBoolean());
            } else {
                throw new IllegalArgumentException("unsupported");
            }
        } else if (je.isJsonArray()) {
            NutsArrayElementBuilder arr = ws.formats().element().elements().forArray();
            for (JsonElement object : je.getAsJsonArray()) {
                arr.add(gsonToElement(object));
            }
            return arr.build();
        } else if (je.isJsonObject()) {
            NutsObjectElementBuilder arr = ws.formats().element().elements().forObject();
            for (Map.Entry<String, JsonElement> e : je.getAsJsonObject().entrySet()) {
                arr.set(e.getKey(), gsonToElement(e.getValue()));
            }
            return arr.build();
        }
        throw new IllegalArgumentException("unsupported");
    }

//    @Override
    public NutsElement parseElement(Reader reader, NutsSession session) {
        return getGson(true).fromJson(reader, NutsElement.class);
    }

////    @Override
//    public <T> T fromObject(Object any, Class<T> clazz) {
//        Gson gson = getGson(true);
//        JsonElement t = gson.toJsonTree(any);
//        return gson.fromJson(t, clazz);
//    }
    public Gson getGson(boolean compact) {
        if (compact) {
            if (GSON_COMPACT == null) {
                GSON_COMPACT = prepareBuilder().create();
            }
            return GSON_COMPACT;
        } else {
            if (GSON_PRETTY == null) {
                GSON_PRETTY = prepareBuilder().setPrettyPrinting().create();
            }
            return GSON_PRETTY;
        }
    }

    public void printElement(NutsElement value, PrintStream out, boolean compact, NutsSession session) {
        getGson(compact).toJson(value, out);
    }

    public JsonElement elementToGson(NutsElement o) {
        switch (o.type()) {
            case BOOLEAN: {
                return new JsonPrimitive(((NutsPrimitiveElement) o).getBoolean());
            }
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
            case FLOAT: 
            case DOUBLE: 
            {
                return new JsonPrimitive(((NutsPrimitiveElement) o).getNumber());
            }
            case STRING: {
                return new JsonPrimitive(((NutsPrimitiveElement) o).getString());
            }
            case INSTANT: {
                return new JsonPrimitive(((NutsPrimitiveElement) o).getString());
            }
            case NULL: {
                return JsonNull.INSTANCE;
            }
            case ARRAY: {
                JsonArray a = new JsonArray();
                for (NutsElement attribute : o.asArray().children()) {
                    a.add(elementToGson(attribute));
                }
                return a;
            }
            case OBJECT: {
                NutsObjectElement obj = o.asObject();
                if (isSimpleObject(obj)) {
                    JsonObject a = new JsonObject();
                    for (NutsElementEntry attribute : obj.children()) {
                        a.add(attribute.getKey().asPrimitive().getString(), elementToGson(attribute.getValue()));
                    }
                    return a;
                } else {
                    JsonArray a = new JsonArray();
                    for (NutsElementEntry attribute : obj.children()) {
                        JsonObject oo = new JsonObject();
                        oo.add("key", elementToGson(attribute.getKey()));
                        oo.add("value", elementToGson(attribute.getValue()));
                        a.add(oo);
                    }
                    return a;
                }
            }
            default: {
                throw new IllegalArgumentException("Unsupported " + o.type());
            }
        }
    }

    public boolean isSimpleObject(NutsObjectElement obj) {
        Set<String> keys = new HashSet<>();
        for (NutsElementEntry attribute : obj.children()) {
            if (attribute.getKey().type() != NutsElementType.STRING) {
                return false;
            }
            final String k = attribute.getKey().asPrimitive().getString();
            if (!keys.contains(k)) {
                keys.add(k);
            }
        }
        return true;
    }

    public GsonBuilder prepareBuilder() {
        GsonBuilder b = new GsonBuilder();
        b.registerTypeHierarchyAdapter(NutsElement.class, new NutsElementGsonAdapter());
        return b;
    }

    private class NutsElementGsonAdapter implements
            com.google.gson.JsonSerializer<NutsElement>,
            com.google.gson.JsonDeserializer<NutsElement> {

        public NutsElementGsonAdapter() {
        }

        @Override
        public NutsElement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return gsonToElement(json);
        }

        @Override
        public JsonElement serialize(NutsElement src, Type typeOfSrc, JsonSerializationContext context) {
            return elementToGson(src);
        }
    }

}
