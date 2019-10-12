/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.format.json;

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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.*;
import net.vpc.app.nuts.runtime.config.DefaultNutsArtifactCallBuilder;
import net.vpc.app.nuts.runtime.config.DefaultNutsDescriptorBuilder;
import net.vpc.app.nuts.runtime.config.DefaultNutsIdLocationBuilder;
import net.vpc.app.nuts.runtime.format.DefaultFormatBase;
import net.vpc.app.nuts.runtime.format.elem.DefaultNutsElementFactoryContext;
import net.vpc.app.nuts.runtime.format.elem.NutsElementFactoryContext;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.format.xml.NutsXmlUtils;
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
    public NutsJsonFormat value(Object value) {
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
    public <T> T parse(URL url, Class<T> clazz) {
        try {
            try (InputStream is = url.openStream()) {
                return parse(is, clazz);
            } catch (NutsException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                throw new NutsParseException(ws, "Unable to parse url " + url, ex);
            }
        } catch (IOException ex) {
            throw new NutsParseException(ws, "Unable to parse url " + url, ex);
        }
    }

    @Override
    public <T> T parse(InputStream inputStream, Class<T> clazz) {
        return parse(new InputStreamReader(inputStream), clazz);
    }

    @Override
    public <T> T parse(byte[] bytes, Class<T> clazz) {
        return parse(new ByteArrayInputStream(bytes), clazz);
    }

    @Override
    public <T> T parse(Reader reader, Class<T> clazz) {
        return getGson(true).fromJson(reader, clazz);
    }

    @Override
    public <T> T parse(Path path, Class<T> clazz) {
        File file = path.toFile();
        try (FileReader r = new FileReader(file)) {
            return parse(r, clazz);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public <T> T parse(File file, Class<T> clazz) {
        try (FileReader r = new FileReader(file)) {
            return parse(r, clazz);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
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

    public JsonElement toJsonElement(NutsElement o) {
        switch (o.type()) {
            case BOOLEAN: {
                return new JsonPrimitive(((NutsPrimitiveElement) o).getBoolean());
            }
            case INTEGER: 
            case FLOAT: 
            {
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
                throw new IllegalArgumentException("Unsupported "+o.type());
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
                .registerTypeHierarchyAdapter(NutsDependency.class, new NutsDependencyJsonAdapter())
                .registerTypeHierarchyAdapter(NutsIdLocation.class, new NutsIdLocationJsonAdapter())
                .registerTypeHierarchyAdapter(NutsClassifierMapping.class, new NutsClassifierMappingJsonAdapter())
                .registerTypeHierarchyAdapter(NutsArtifactCall.class, new NutsExecutorDescriptorAdapter())
                .registerTypeHierarchyAdapter(NutsDependencyTreeNode.class, new NutsDependencyTreeNodeJsonAdapter())
                .registerTypeHierarchyAdapter(NutsElement.class, new NutsElementJsonAdapter())
                .registerTypeHierarchyAdapter(org.w3c.dom.Element.class, new XmlElementJsonAdapter())
                .registerTypeHierarchyAdapter(org.w3c.dom.Document.class, new XmlDocumentJsonAdapter())
                .registerTypeHierarchyAdapter(Path.class, new PathJsonAdapter())
                .registerTypeHierarchyAdapter(File.class, new FileJsonAdapter())
                .registerTypeHierarchyAdapter(Date.class, new DateJsonAdapter())
                .registerTypeHierarchyAdapter(Instant.class, new InstantJsonAdapter());
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
            return NutsWorkspaceUtils.parseRequiredNutsId0( s);
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
                return context.serialize(new DefaultNutsDescriptorBuilder().set(src));
            }
            return context.serialize(src);
        }
    }

    private static class NutsDependencyTreeNodeJsonAdapter implements
            com.google.gson.JsonSerializer<NutsDependencyTreeNode>,
            com.google.gson.JsonDeserializer<NutsDependencyTreeNode> {

        @Override
        public NutsDependencyTreeNode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return context.deserialize(json, MutableNutsDependencyTreeNode.class);
        }

        @Override
        public JsonElement serialize(NutsDependencyTreeNode src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
                return context.serialize(new MutableNutsDependencyTreeNode(src));
            }
            return context.serialize(src);
        }
    }

    private static class NutsDependencyJsonAdapter implements
            com.google.gson.JsonSerializer<NutsDependency>,
            com.google.gson.JsonDeserializer<NutsDependency> {

        @Override
        public NutsDependency deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String b = context.deserialize(json, String.class);
            return CoreNutsUtils.parseNutsDependency(null,b);
        }

        @Override
        public JsonElement serialize(NutsDependency src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
                return context.serialize(src.toString());
            }
            return context.serialize(src);
        }
    }

    private static class NutsIdLocationJsonAdapter implements
            com.google.gson.JsonSerializer<NutsIdLocation>,
            com.google.gson.JsonDeserializer<NutsIdLocation> {

        @Override
        public NutsIdLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            NutsIdLocationBuilder b = context.deserialize(json, DefaultNutsIdLocationBuilder.class);
            return b.build();
        }

        @Override
        public JsonElement serialize(NutsIdLocation src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
                return context.serialize(new DefaultNutsIdLocationBuilder(src));
            }
            return context.serialize(src);
        }
    }

    private static class NutsClassifierMappingJsonAdapter implements
            com.google.gson.JsonSerializer<NutsClassifierMapping>,
            com.google.gson.JsonDeserializer<NutsClassifierMapping> {

        @Override
        public NutsClassifierMapping deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            NutsClassifierMappingBuilder b = context.deserialize(json, DefaultNutsClassifierMappingBuilder.class);
            return b.build();
        }

        @Override
        public JsonElement serialize(NutsClassifierMapping src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
                return context.serialize(new DefaultNutsClassifierMappingBuilder().set(src));
            }
            return context.serialize(src);
        }
    }

    private static class NutsExecutorDescriptorAdapter implements
            com.google.gson.JsonSerializer<NutsArtifactCall>,
            com.google.gson.JsonDeserializer<NutsArtifactCall> {

        @Override
        public NutsArtifactCall deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            NutsArtifactCallBuilder b = context.deserialize(json, DefaultNutsArtifactCallBuilder.class);
            return b.build();
        }

        @Override
        public JsonElement serialize(NutsArtifactCall src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
                return context.serialize(new DefaultNutsArtifactCallBuilder(src));
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
            return ws.xml().toXmlElement(fromJsonElement(json), null);
        }

        @Override
        public JsonElement serialize(org.w3c.dom.Element src, Type typeOfSrc, JsonSerializationContext context) {
            return toJsonElement(ws.xml().fromXmlElement(src, NutsElement.class));
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
            Element ee = ws.xml().toXmlElement(fromJsonElement(json), doc);
            ee = (Element) doc.importNode(ee, true);
            doc.appendChild(ee);
            return doc;
        }

        @Override
        public JsonElement serialize(org.w3c.dom.Document src, Type typeOfSrc, JsonSerializationContext context) {
            NutsElement element = ws.xml().fromXmlElement(src.getDocumentElement(), NutsElement.class);
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

    private class DateJsonAdapter implements
            com.google.gson.JsonSerializer<Date>,
            com.google.gson.JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new Date(dummyContext.builder().forDate(json.getAsString()).primitive().getDate().toEpochMilli());
        }

        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.toInstant().toString());
        }
    }

    private class InstantJsonAdapter implements
            com.google.gson.JsonSerializer<Instant>,
            com.google.gson.JsonDeserializer<Instant> {

        @Override
        public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return dummyContext.builder().forDate(json.getAsString()).primitive().getDate();
        }

        @Override
        public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.toString());
        }
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        return false;
    }

}
