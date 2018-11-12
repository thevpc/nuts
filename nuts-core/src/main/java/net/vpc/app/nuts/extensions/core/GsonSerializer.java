package net.vpc.app.nuts.extensions.core;

import com.google.gson.*;
import net.vpc.app.nuts.JsonSerializer;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.common.io.RuntimeIOException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;

public class GsonSerializer implements JsonSerializer {
    public static final GsonSerializer INSTANCE = new GsonSerializer();
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
                .registerTypeHierarchyAdapter(NutsDescriptor.class, new NutsDescriptorJsonAdapter())
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
}
