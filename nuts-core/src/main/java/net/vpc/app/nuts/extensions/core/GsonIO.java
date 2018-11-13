package net.vpc.app.nuts.extensions.core;

import com.google.gson.*;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.common.io.RuntimeIOException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;

public class GsonIO implements JsonIO {
    public static final GsonIO INSTANCE = new GsonIO();
    public static final Gson GSON = prepareBuilder()
            .create();
    public static final Gson GSON_PRETTY = prepareBuilder()
            .setPrettyPrinting()
            .create();

    public static GsonBuilder prepareBuilder() {
        return new GsonBuilder()
//                .registerTypeHierarchyAdapter(NutsDescriptor.class, new JsonDeserializer<NutsDescriptor>() {
//                    @Override
//                    public NutsDescriptor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//                        return context.deserialize(json,DefaultNutsDescriptor.class);
//                    }
//                })
                .registerTypeHierarchyAdapter(NutsId.class, new NutsIdJsonAdapter())
                .registerTypeHierarchyAdapter(NutsVersion.class, new NutsVersionJsonAdapter())
                .registerTypeHierarchyAdapter(NutsDescriptor.class, new NutsDescriptorJsonAdapter())
                .registerTypeHierarchyAdapter(NutsDependency.class, new NutsNutsDependencyJsonAdapter())
                ;
    }

    public static Gson getGson(boolean pretty) {
        return pretty ? GSON_PRETTY : GSON;
    }

    @Override
    public void write(Object obj, Writer out, boolean pretty) {
        getGson(pretty).toJson(obj, out);
        try {
            out.flush();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    @Override
    public <T> T read(Reader reader, Class<T> cls) {
        return GSON.fromJson(reader, cls);
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
            return CoreNutsUtils.parseOrErrorNutsId(s);
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
            return new NutsVersionImpl(s);
        }


        @Override
        public JsonElement serialize(NutsVersion src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src == null ? null : src.toString());
        }
    }

    private static class NutsDescriptorJsonAdapter implements
            com.google.gson.JsonSerializer<NutsDescriptor>,
            com.google.gson.JsonDeserializer<NutsDescriptor>
    {

        @Override
        public NutsDescriptor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            DefaultNutsDescriptorBuilder b = context.deserialize(json, DefaultNutsDescriptorBuilder.class);
            return b.build();
        }


        @Override
        public JsonElement serialize(NutsDescriptor src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null ) {
                return context.serialize(new DefaultNutsDescriptorBuilder(src));
            }
            return context.serialize(src);
        }
    }
    private static class NutsNutsDependencyJsonAdapter implements
            com.google.gson.JsonSerializer<NutsDependency>,
            com.google.gson.JsonDeserializer<NutsDependency>
    {

        @Override
        public NutsDependency deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            DefaultNutsDependencyBuilder b = context.deserialize(json, DefaultNutsDependencyBuilder.class);
            return b.build();
        }


        @Override
        public JsonElement serialize(NutsDependency src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null ) {
                return context.serialize(new DefaultNutsDependencyBuilder(src));
            }
            return context.serialize(src);
        }
    }
}
