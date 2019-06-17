/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format.json;

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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsDependencyBuilder;
import net.vpc.app.nuts.core.DefaultNutsDescriptorBuilder;
import net.vpc.app.nuts.core.DefaultNutsVersion;
import net.vpc.app.nuts.core.format.DefaultFormatBase;
import net.vpc.app.nuts.core.format.elem.DefaultNutsElementFactoryContext;
import net.vpc.app.nuts.core.format.elem.NutsElementFactoryContext;
import net.vpc.app.nuts.core.format.elem.NutsElementUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.format.xml.NutsXmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author vpc
 */
public class DefaultNutsJsonFormat extends DefaultFormatBase<NutsJsonFormat> implements NutsJsonFormat {

    private Gson GSON_COMPACT;
    private Gson GSON_PRETTY;
    private boolean compact;
    private Object value;
    private final NutsElementFactoryContext dummyContext;

    public DefaultNutsJsonFormat(NutsWorkspace ws) {
        super(ws, "json-format");
        dummyContext = new DefaultNutsElementFactoryContext(ws) {
            @Override
            public NutsElement toElement(Object o) {
                return fromJsonElement((JsonElement) o);
            }

        };
    }

    @Override
    public boolean isCompact() {
        return compact;
    }

    @Override
    public NutsJsonFormat compact() {
        return compact(true);
    }

    @Override
    public NutsJsonFormat compact(boolean compact) {
        return setCompact(compact);
    }

    @Override
    public NutsJsonFormat setCompact(boolean compact) {
        this.compact = compact;
        return this;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public NutsJsonFormat set(Object value) {
        return setValue(value);
    }

    @Override
    public NutsJsonFormat setValue(Object value) {
        this.value = value;
        return this;
    }

    public <T> T convert(Object any, Class<T> to) {
        Gson gson = getGson(true);
        JsonElement t = gson.toJsonTree(any);
        return gson.fromJson(t, to);
    }

    @Override
    public void print(Writer out) {
        getGson(compact).toJson(value, out);
        try {
            out.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public <T> T parse(Reader reader, Class<T> cls) {
        return getGson(true).fromJson(reader, cls);
    }

    @Override
    public <T> T parse(Path path, Class<T> cls) {
        File file = path.toFile();
        try (FileReader r = new FileReader(file)) {
            return parse(r, cls);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public <T> T parse(File file, Class<T> cls) {
        try (FileReader r = new FileReader(file)) {
            return parse(r, cls);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public NutsElement fromJsonElement(JsonElement o) {
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
            return new NutsArrayElementJson(je.getAsJsonArray(), dummyContext);
        } else if (je.isJsonObject()) {
            return new NutsObjectElementJson((je.getAsJsonObject()), dummyContext);
        }
        throw new IllegalArgumentException("Unsupported");
    }

    public JsonElement toJsonElement(NutsElement o) {
        switch (o.type()) {
            case BOOLEAN: {
                return new JsonPrimitive(((NutsPrimitiveElement) o).getBoolean());
            }
            case NUMBER: {
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
            case UNKNWON: {
                return new JsonPrimitive(((NutsPrimitiveElement) o).getString());
            }
            default: {
                throw new IllegalArgumentException("Unsupported");
            }
        }
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

    public GsonBuilder prepareBuilder() {
        return new GsonBuilder()
                .registerTypeHierarchyAdapter(NutsId.class, new NutsIdJsonAdapter())
                .registerTypeHierarchyAdapter(NutsVersion.class, new NutsVersionJsonAdapter())
                .registerTypeHierarchyAdapter(NutsDescriptor.class, new NutsDescriptorJsonAdapter())
                .registerTypeHierarchyAdapter(NutsDependency.class, new NutsNutsDependencyJsonAdapter())
                .registerTypeHierarchyAdapter(NutsElement.class, new NutsElementJsonAdapter())
                .registerTypeHierarchyAdapter(org.w3c.dom.Element.class, new XmlElementJsonAdapter())
                .registerTypeHierarchyAdapter(org.w3c.dom.Document.class, new XmlDocumentJsonAdapter())
                .registerTypeHierarchyAdapter(Path.class, new PathJsonAdapter())
                .registerTypeHierarchyAdapter(File.class, new FileJsonAdapter());
    }

    private static class NutsIdJsonAdapter implements
            com.google.gson.JsonSerializer<NutsId>,
            com.google.gson.JsonDeserializer<NutsId> {

        @Override
        public NutsId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String s = context.deserialize(json, String.class);
            if (s == null) {
                return null;
            }
            return NutsWorkspaceUtils.parseRequiredNutsId(null, s);
        }

        @Override
        public JsonElement serialize(NutsId src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src == null ? null : src.toString());
        }
    }

    private static class NutsVersionJsonAdapter implements
            com.google.gson.JsonSerializer<NutsVersion>,
            com.google.gson.JsonDeserializer<NutsVersion> {

        @Override
        public NutsVersion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String s = context.deserialize(json, String.class);
            if (s == null) {
                return null;
            }
            return DefaultNutsVersion.valueOf(s);
        }

        @Override
        public JsonElement serialize(NutsVersion src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src == null ? null : src.toString());
        }
    }

    private static class NutsDescriptorJsonAdapter implements
            com.google.gson.JsonSerializer<NutsDescriptor>,
            com.google.gson.JsonDeserializer<NutsDescriptor> {

        @Override
        public NutsDescriptor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            DefaultNutsDescriptorBuilder b = context.deserialize(json, DefaultNutsDescriptorBuilder.class);
            return b.build();
        }

        @Override
        public JsonElement serialize(NutsDescriptor src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
                return context.serialize(new DefaultNutsDescriptorBuilder(src));
            }
            return context.serialize(src);
        }
    }

    private static class NutsNutsDependencyJsonAdapter implements
            com.google.gson.JsonSerializer<NutsDependency>,
            com.google.gson.JsonDeserializer<NutsDependency> {

        @Override
        public NutsDependency deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            DefaultNutsDependencyBuilder b = context.deserialize(json, DefaultNutsDependencyBuilder.class);
            return b.build();
        }

        @Override
        public JsonElement serialize(NutsDependency src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
                return context.serialize(new DefaultNutsDependencyBuilder(src));
            }
            return context.serialize(src);
        }
    }

    private class NutsElementJsonAdapter implements
            com.google.gson.JsonSerializer<NutsElement>,
            com.google.gson.JsonDeserializer<NutsElement> {

        @Override
        public NutsElement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return fromJsonElement(json);
        }

        @Override
        public JsonElement serialize(NutsElement src, Type typeOfSrc, JsonSerializationContext context) {
            return toJsonElement(src);
        }
    }

    private class XmlElementJsonAdapter implements
            com.google.gson.JsonSerializer<org.w3c.dom.Element>,
            com.google.gson.JsonDeserializer<org.w3c.dom.Element> {

        @Override
        public org.w3c.dom.Element deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return ws.format().xml().toXmlElement(fromJsonElement(json), null);
        }

        @Override
        public JsonElement serialize(org.w3c.dom.Element src, Type typeOfSrc, JsonSerializationContext context) {
            return toJsonElement(ws.format().xml().fromXmlElement(src, NutsElement.class));
        }
    }

    private class XmlDocumentJsonAdapter implements
            com.google.gson.JsonSerializer<org.w3c.dom.Document>,
            com.google.gson.JsonDeserializer<org.w3c.dom.Document> {

        @Override
        public org.w3c.dom.Document deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Document doc;
            try {
                doc = NutsXmlUtils.createDocument();
            } catch (ParserConfigurationException ex) {
                throw new JsonParseException(ex.getMessage(), ex);
            }
            Element ee = ws.format().xml().toXmlElement(fromJsonElement(json), doc);
            ee = (Element) doc.importNode(ee, true);
            doc.appendChild(ee);
            return doc;
        }

        @Override
        public JsonElement serialize(org.w3c.dom.Document src, Type typeOfSrc, JsonSerializationContext context) {
            NutsElement element = ws.format().xml().fromXmlElement(src.getDocumentElement(), NutsElement.class);
            return toJsonElement(element);
        }
    }

    private class PathJsonAdapter implements
            com.google.gson.JsonSerializer<Path>,
            com.google.gson.JsonDeserializer<Path> {

        @Override
        public Path deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Paths.get(json.getAsString());
        }

        @Override
        public JsonElement serialize(Path src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.toString());
        }
    }

    private class FileJsonAdapter implements
            com.google.gson.JsonSerializer<File>,
            com.google.gson.JsonDeserializer<File> {

        @Override
        public File deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new File(json.getAsString());
        }

        @Override
        public JsonElement serialize(File src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.getPath());
        }
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        return false;
    }

}
