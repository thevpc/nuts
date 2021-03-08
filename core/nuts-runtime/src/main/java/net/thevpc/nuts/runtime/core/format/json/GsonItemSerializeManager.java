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
package net.thevpc.nuts.runtime.core.format.json;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementBuilder;
import net.thevpc.nuts.NutsElementType;
import net.thevpc.nuts.NutsNamedElement;
import net.thevpc.nuts.NutsPrimitiveElement;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.elem.DefaultNutsElementFactoryContext;
import net.thevpc.nuts.runtime.core.format.elem.DefaultNutsNamedElement;
import net.thevpc.nuts.runtime.core.format.elem.NutsElementFactoryContext;
import net.thevpc.nuts.runtime.core.format.elem.NutsObjectElementBase;

/**
 *
 * @author vpc
 */
public class GsonItemSerializeManager implements ItemSerializeManager {

    private Gson GSON_COMPACT;
    private Gson GSON_PRETTY;
    private NutsWorkspace ws;
    private NutsElementFactoryContext dummyContext;
    private Supplier<NutsSession> sessionSupplier;
    private Map<Class, NutsElemenSerializationAdapter> adapters = new HashMap<>();

    public GsonItemSerializeManager(NutsWorkspace ws, Supplier<NutsSession> sessionSupplier) {
        this.ws = ws;
        this.sessionSupplier = sessionSupplier;
        this.dummyContext = new DefaultNutsElementFactoryContext(ws) {
            @Override
            public NutsElement toElement(Object o) {
                return fromJsonElement((JsonElement) o);
            }

        };;
    }

    public ItemSerializeManager setAdapter(Class cls, NutsElemenSerializationAdapter a) {
        if (a == null) {
            adapters.remove(cls);
        } else {
            adapters.put(cls, a);
        }
        return this;
    }

    public NutsElement fromJsonElement(JsonElement o) {
        JsonElement je = (JsonElement) o;
        if (je.isJsonNull()) {
            return dummyContext.builder().forNull();
        } else if (je.isJsonPrimitive()) {
            JsonPrimitive jr = je.getAsJsonPrimitive();
            if (jr.isString()) {
                return dummyContext.builder().forString(jr.getAsString());
            } else if (jr.isNumber()) {
                return dummyContext.builder().forNumber(jr.getAsNumber());
            } else if (jr.isBoolean()) {
                return dummyContext.builder().forBoolean(jr.getAsBoolean());
            } else {
                throw new IllegalArgumentException("Unsupported");
            }
        } else if (je.isJsonArray()) {
            return new NutsArrayElementJson(je.getAsJsonArray(), dummyContext);
        } else if (je.isJsonObject()) {
            return new NutsObjectElementJson((je.getAsJsonObject()), dummyContext);
        }
        throw new IllegalArgumentException("Unsupported");
    }

    @Override
    public <T> T fromJson(Reader reader, Class<T> clazz) {
        return getGson(true).fromJson(reader, clazz);
    }

    @Override
    public <T> T fromObject(Object any, Class<T> clazz) {
        Gson gson = getGson(true);
        JsonElement t = gson.toJsonTree(any);
        return gson.fromJson(t, clazz);
    }

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

    public void print(Object value, PrintStream out, boolean compact) {
        getGson(compact).toJson(value, out);
    }

    public JsonElement toJsonElement(NutsElement o) {
        switch (o.type()) {
            case BOOLEAN: {
                return new JsonPrimitive(((NutsPrimitiveElement) o).getBoolean());
            }
            case INTEGER:
            case FLOAT: {
                return new JsonPrimitive(((NutsPrimitiveElement) o).getNumber());
            }
            case STRING: {
                return new JsonPrimitive(((NutsPrimitiveElement) o).getString());
            }
            case DATE: {
                return new JsonPrimitive(((NutsPrimitiveElement) o).getString());
            }
            case NULL: {
                return JsonNull.INSTANCE;
            }
            case ARRAY: {
                JsonArray a = new JsonArray();
                for (NutsElement attribute : o.array().children()) {
                    a.add(toJsonElement(attribute));
                }
                return a;
            }
            case OBJECT: {
                JsonObject a = new JsonObject();
                Set<String> visited = new HashSet<String>();
                for (NutsNamedElement attribute : o.object().children()) {
                    String k = attribute.getName();
                    if (visited.contains(k)) {
                        throw new IllegalArgumentException("Unexpected");
                    }
                    visited.add(k);
                    a.add(k, toJsonElement(attribute.getValue()));
                }
                return a;
            }
            default: {
                throw new IllegalArgumentException("Unsupported " + o.type());
            }
        }
    }

    public GsonBuilder prepareBuilder() {
        GsonBuilder b = new GsonBuilder();
        for (Map.Entry<Class, NutsElemenSerializationAdapter> entry : adapters.entrySet()) {
            b.registerTypeHierarchyAdapter(entry.getKey(), new HelperAdapter(entry.getValue(), ws));
        }
        return b;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    private class GsonElemenDeserializationContext implements NutsElementDeserializationContext {

        JsonDeserializationContext context;

        public GsonElemenDeserializationContext(JsonDeserializationContext context) {
            this.context = context;
        }

        @Override
        public <T> T getAs(Object o, Class<T> c) {
//            if (c.equals(String.class)) {
//                return (T) ((JsonElement) o).getAsString();
//            }
            return context.deserialize(((JsonElement) o), c);
        }

        @Override
        public NutsElementBuilder elements() {
            return dummyContext.builder();
        }

        @Override
        public NutsWorkspace workspace() {
            return ws;
        }

        @Override
        public NutsSession session() {
            return sessionSupplier.get();
        }

    }

    private class GsonElemenSerializationContext implements NutsElementSerializationContext {

        JsonSerializationContext context;

        public GsonElemenSerializationContext(JsonSerializationContext context) {
            this.context = context;
        }

        @Override
        public NutsElementBuilder elements() {
            return dummyContext.builder();
        }

        @Override
        public NutsWorkspace workspace() {
            return ws;
        }

        @Override
        public NutsElement serialize(Object other) {
            JsonElement json = context.serialize(other);
            return fromJsonElement(json);
        }

    }

    private class HelperAdapter<T> implements
            com.google.gson.JsonSerializer<T>,
            com.google.gson.JsonDeserializer<T> {

        private NutsWorkspace ws;
        private NutsElemenSerializationAdapter<T> a;

        public HelperAdapter(NutsElemenSerializationAdapter<T> a, NutsWorkspace ws) {
            this.ws = ws;
            this.a = a;
        }

        @Override
        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return a.deserialize(json, typeOfT, new GsonElemenDeserializationContext(context));
        }

        @Override
        public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
            NutsElement e = a.serialize(src, typeOfSrc, new GsonElemenSerializationContext(context));
            return toJsonElement(e);
        }
    }
    
    
    
/**
 *
 * @author thevpc
 */
public static class NutsObjectElementJson extends NutsObjectElementBase {

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

}
