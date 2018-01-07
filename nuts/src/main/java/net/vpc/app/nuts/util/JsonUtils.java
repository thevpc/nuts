/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.util;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by vpc on 1/12/17.
 */
public class JsonUtils {

    public static final JsonUtils.SerializeOptions IGNORE_EMPTY_OPTIONS = new JsonUtils.SerializeOptions()
            .setIgnoreNulls(true)
            .setIgnoreEmptyStrings(true)
            .setIgnoreEmptyCollections(true)
            .setIgnoreEmptyMaps(true)
            .setIgnoreEmptyArrays(true);
    public static final JsonUtils.SerializeOptions PRETTY_IGNORE_EMPTY_OPTIONS = new JsonUtils.SerializeOptions()
            .setIgnoreNulls(true)
            .setIgnoreEmptyStrings(true)
            .setIgnoreEmptyCollections(true)
            .setIgnoreEmptyMaps(true)
            .setIgnoreEmptyArrays(true)
            .setPretty(true);

    public static boolean isNull(JsonValue obj) {
        return obj == null || JsonValue.NULL.equals(obj);
    }

    public static Map<String, String> deserializeStringsMap(JsonValue obj, Map<String, String> t) {
        if (obj == null) {
            return null;
        }
        if (JsonValue.NULL.equals(obj)) {
            return null;
        }
        JsonObject a = (JsonObject) obj;
        for (Map.Entry<String, JsonValue> entry : a.entrySet()) {
            t.put(entry.getKey(), (String) deserialize(entry.getValue(), String.class));
        }
        return t;
    }

    public static <T> T deserialize(String s, Class<T> t) {
        return deserialize(new ByteArrayInputStream(s.getBytes()), t);
    }

    public static <T> T deserialize(InputStream s, Class<T> t) {
        JsonStructure jsonObject = Json.createReader(s).read();
        return deserialize(jsonObject, t);
    }

    public static <T> T deserialize(JsonValue obj, Class<T> t) {
        if (obj == null) {
            return null;
        }
        if (JsonValue.NULL.equals(obj)) {
            return null;
        }
        if (t == null) {
            if (obj instanceof JsonString) {
                t = (Class<T>) String.class;
            } else {
                throw new IllegalArgumentException("Unable to resolve null type for " + obj);
            }
        }
        if (t.equals(String.class)) {
            return (T) ((JsonString) obj).getString();
        } else if (t.equals(Integer.class) || t.equals(Integer.TYPE)) {
            return (T) (Integer) ((JsonNumber) obj).intValue();
        } else if (t.equals(Long.class) || t.equals(Long.TYPE)) {
            return (T) (Long) ((JsonNumber) obj).longValue();
        } else if (t.equals(Boolean.class) || t.equals(Boolean.TYPE)) {
            return (T) Boolean.valueOf(JsonValue.TRUE.equals(obj));
        } else if (t.isArray()) {
            JsonArray a = (JsonArray) obj;
            Object arr = Array.newInstance(t.getComponentType(), a.size());
            for (int i = 0; i < a.size(); i++) {
                Array.set(arr, i, deserialize(a.get(i), t.getComponentType()));
            }
            return (T) arr;
        } else if (List.class.isAssignableFrom(t)) {
            JsonArray a = (JsonArray) obj;
            List arr = new ArrayList();
            for (int i = 0; i < a.size(); i++) {
                arr.add(i, deserialize(a.get(i), null));
            }
            return (T) arr;
        } else if (Set.class.isAssignableFrom(t)) {
            JsonArray a = (JsonArray) obj;
            LinkedHashSet arr = new LinkedHashSet();
            for (JsonValue anA : a) {
                arr.add(deserialize(anA, null));
            }
            return (T) arr;
        } else if (Properties.class.isAssignableFrom(t)) {
            JsonObject a = (JsonObject) obj;
            Properties arr = new Properties();
            for (Map.Entry<String, JsonValue> entry : a.entrySet()) {
                arr.setProperty(entry.getKey(), (String) deserialize(entry.getValue(), String.class));
            }
            return (T) arr;
        } else {
            if (!(obj instanceof JsonObject)) {
                throw new ClassCastException("Expected json object but get " + obj);
            }
            JsonObject a = (JsonObject) obj;
            Object ooo = null;
            try {
                ooo = t.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try {
                Field instanceSerialVersionUID = null;
                instanceSerialVersionUID = t.getDeclaredField("instanceSerialVersionUID");
                if (instanceSerialVersionUID.getType().equals(Long.TYPE) && !Modifier.isStatic(instanceSerialVersionUID.getModifiers())) {
                    instanceSerialVersionUID.setAccessible(true);
                    JsonNumber serialVersionUID = a.getJsonNumber("serialVersionUID");
                    if (serialVersionUID != null) {
                        instanceSerialVersionUID.set(null, serialVersionUID.longValue());
                    }
                }
            } catch (Exception e) {
                //
            }
            for (Map.Entry<String, JsonValue> entry : a.entrySet()) {
                String key = entry.getKey();
                if (key.equals("serialVersionUID")) {
                    key = "instanceSerialVersionUID";
                }
                PlatformBeanProperty pp = PlatformUtils.findPlatformBeanProperty(key, t);
                if (pp != null) {
                    pp.setValue(ooo, deserialize(entry.getValue(), pp.getPlatformType()));
                }
            }
            return (T) ooo;
        }
    }

    /**
     * cyclic ref is not supported !!!
     *
     * @param obj
     */
    public static JsonObjectBuilder serializeObj(Object obj, SerializeOptions options) {
        if (obj instanceof Properties) {
            Properties props = (Properties) obj;
            if (!props.isEmpty() || !options.ignoreEmptyMaps) {
                JsonObjectBuilder propBuilder = Json.createObjectBuilder();
                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    serializeObjProp((String) entry.getKey(), (String) entry.getValue(), String.class, propBuilder, options);
                }
                return propBuilder;
            }
            return null;
        }
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        Class<?> type = obj.getClass();
        try {
            Field serialVersionUID = null;
            serialVersionUID = type.getDeclaredField("serialVersionUID");
            if (serialVersionUID.getType().equals(Long.TYPE)
                    && Modifier.isStatic(serialVersionUID.getModifiers())
                    && !Modifier.isTransient(serialVersionUID.getModifiers())
                    && serialVersionUID.getAnnotation(JsonTransient.class)==null
                    ) {
                serialVersionUID.setAccessible(true);
                serializeObjProp("serialVersionUID", serialVersionUID.get(null), Long.TYPE, objectBuilder, options);
            }
        } catch (Exception e) {
            //
        }
        for (PlatformBeanProperty property : PlatformUtils.findPlatformBeanProperties(type)) {
            if (property.isReadSupported() && !property.isDeprecated() && !property.isTransient() && !"instanceSerialVersionUID".equals(property.getName())) {
                Class t = property.getPlatformType();
                Object value = property.getValue(obj);
                serializeObjProp(property.getName(), value, t, objectBuilder, options);
            }
        }
        return objectBuilder;
    }

    public static JsonArrayBuilder serializeArr(Object obj, SerializeOptions options) {
        if (obj.getClass().isArray()) {
            JsonArrayBuilder propBuilder = Json.createArrayBuilder();
            int max = Array.getLength(obj);
            if (max > 0 || !options.ignoreEmptyArrays) {
                for (int index = 0; index < max; index++) {
                    serializeArrProp(Array.get(obj, index), obj.getClass().getComponentType(), propBuilder, options);
                }
                return propBuilder;
            } else {
                return null;
            }
        }
        if (Collection.class.isAssignableFrom(obj.getClass())) {
            JsonArrayBuilder propBuilder = Json.createArrayBuilder();
            Collection col = (Collection) obj;
            int max = col.size();
            if (max > 0 || !options.ignoreEmptyArrays) {
                int index = 0;
                for (Object o : col) {
                    serializeArrProp(o, o == null ? null : o.getClass(), propBuilder, options);
                    index++;
                }
                return propBuilder;
            } else {
                return null;
            }
        }
        throw new IllegalArgumentException("Unsupported");
    }

    public static JsonObjectBuilder serializeStringsMap(Map<String, String> value, SerializeOptions options) {
        if (value != null && (!value.isEmpty() || !options.ignoreEmptyMaps)) {
            JsonObjectBuilder propBuilder = Json.createObjectBuilder();
            for (Map.Entry<String, String> entry : value.entrySet()) {
                serializeObjProp(entry.getKey(), entry.getValue(), String.class, propBuilder, options);
            }
            return propBuilder;
        }
        return null;
    }

    public static void serializeObjProp(String prop, Object value, Class t, JsonObjectBuilder builder, SerializeOptions options) {
        if (value != null) {
            if (t.equals(String.class)) {
                String s = (String) value;
                if (!StringUtils.isEmpty(s) || !options.ignoreEmptyStrings) {
                    builder.add(prop, s);
                }
            } else if (t.equals(Integer.class) || t.equals(Integer.TYPE)) {
                builder.add(prop, (Integer) value);
            } else if (t.equals(Long.class) || t.equals(Long.TYPE)) {
                builder.add(prop, (Long) value);
            } else if (t.equals(Double.class) || t.equals(Double.TYPE)) {
                builder.add(prop, (Double) value);
            } else if (t.equals(Boolean.class) || t.equals(Boolean.TYPE)) {
                builder.add(prop, ((Boolean) value) ? JsonValue.TRUE : JsonValue.FALSE);
            } else if (t.isArray()) {
                JsonArrayBuilder propBuilder = Json.createArrayBuilder();
                int max = Array.getLength(value);
                if (max > 0 || !options.ignoreEmptyArrays) {
                    for (int index = 0; index < max; index++) {
                        serializeArrProp(Array.get(value, index), t.getComponentType(), propBuilder, options);
                    }
                    builder.add(prop, propBuilder);
                }
            } else if (Properties.class.isAssignableFrom(t)) {
                Properties props = (Properties) value;
                if (!props.isEmpty() || !options.ignoreEmptyMaps) {
                    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
                    for (Map.Entry<Object, Object> entry : props.entrySet()) {
                        serializeObjProp((String) entry.getKey(), (String) entry.getValue(), String.class, propBuilder, options);
                    }
                    builder.add(prop, propBuilder);
                }
            } else if (Collection.class.isAssignableFrom(t)) {
                Collection coll = (Collection) value;
                if (!coll.isEmpty() || !options.ignoreEmptyCollections) {
                    JsonArrayBuilder propBuilder = Json.createArrayBuilder();
                    for (Object entry : coll) {
                        serializeArrProp(entry, null, propBuilder, options);
                    }
                    builder.add(prop, propBuilder);
                }
            } else {
                JsonObjectBuilder propBuilder = serializeObj(value, options);
                builder.add(prop, propBuilder);
            }
        } else {
            if (!options.ignoreNulls) {
                builder.add(prop, JsonObject.NULL);
            }
        }
    }

    public static void serializeArrProp(Object value, Class t, JsonArrayBuilder builder, SerializeOptions options) {
        if (value != null) {
            if (t == null) {
                t = value.getClass();
            }
            if (t.equals(String.class)) {
                String s = (String) value;
                if (!StringUtils.isEmpty(s) || !options.ignoreEmptyStrings) {
                    builder.add(s);
                }
            } else if (t.equals(Integer.class) || t.equals(Integer.TYPE)) {
                builder.add((Integer) value);
            } else if (t.equals(Long.class) || t.equals(Long.TYPE)) {
                builder.add((Integer) value);
            } else if (t.equals(Boolean.class) || t.equals(Boolean.TYPE)) {
                builder.add(((Boolean) value) ? JsonValue.TRUE : JsonValue.FALSE);
            } else if (t.isArray()) {
                JsonArrayBuilder propBuilder = Json.createArrayBuilder();
                int max = Array.getLength(value);
                if (max > 0 || !options.ignoreEmptyArrays) {
                    for (int index = 0; index < max; index++) {
                        serializeArrProp(Array.get(value, index), t.getComponentType(), builder, options);
                    }
                    builder.add(propBuilder);
                }
            } else if (Properties.class.isAssignableFrom(t)) {
                JsonObjectBuilder propBuilder = Json.createObjectBuilder();
                Properties maps = (Properties) value;
                if (!maps.isEmpty() || !options.ignoreEmptyMaps) {
                    for (Map.Entry<Object, Object> entry : maps.entrySet()) {
                        serializeObjProp((String) entry.getKey(), (String) entry.getValue(), String.class, propBuilder, options);
                    }
                    builder.add(propBuilder);
                }
            } else if (Collection.class.isAssignableFrom(t)) {
                JsonArrayBuilder propBuilder = Json.createArrayBuilder();
                Collection coll = (Collection) value;
                if (!coll.isEmpty() || !options.ignoreEmptyCollections) {
                    for (Object entry : coll) {
                        serializeArrProp(entry, null, propBuilder, options);
                    }
                    builder.add(propBuilder);
                }
            } else {
                builder.add(serializeObj(value, options));
            }
        } else {
            if (!options.ignoreNulls) {
                builder.add(JsonObject.NULL);
            }
        }
    }

    public static <T> T loadJson(String jsonText, Class<T> cls) throws IOException {
        try {
            if (jsonText == null) {
                jsonText = "";
            }
            Reader reader = null;
            try {
                reader = new StringReader(jsonText);
                return (T) deserialize(Json.createReader(reader).read(), cls);
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (Exception ex) {
            throw new IOException("Error Parsing file " + jsonText, ex);
        }
    }

    public static JsonStructure loadJsonStructure(String jsonText) throws IOException {
        try {
            if (jsonText == null) {
                jsonText = "";
            }
            Reader reader = null;
            try {
                reader = new StringReader(jsonText);
                return Json.createReader(reader).read();
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (Exception ex) {
            throw new IOException("Error Parsing string " + jsonText, ex);
        }
    }

    public static <T> T loadJson(File file, Class<T> cls) throws IOException {
        if (!file.exists()) {
            return null;
        }
        try {
            FileReader reader = null;
            try {
                reader = new FileReader(file);
                return (T) deserialize(Json.createReader(reader).read(), cls);
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (Exception ex) {
            throw new IOException("Error Parsing file " + file.getPath(), ex);
        }
    }

    public static void storeJson(JsonStructure structure, File file, boolean pretty) throws IOException {
        File parentFile = file.getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            storeJson(structure, writer, pretty);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static void storeJson(JsonStructure structure, Writer writer, boolean pretty) throws IOException {
        if (pretty) {
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(JsonGenerator.PRETTY_PRINTING, true);
            JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
            JsonWriter jsonWriter = writerFactory.createWriter(writer);

            jsonWriter.write(structure);
            jsonWriter.close();
        } else {
            JsonWriter jsonWriter = Json.createWriter(writer);
            jsonWriter.write(structure);
            jsonWriter.close();
        }
    }

    public static <T> void storeJson(T obj, File file, SerializeOptions options) throws IOException {
        try {
            JsonObjectBuilder b = serializeObj(obj, options);
            storeJson(b.build(), file, options.isPretty());
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public static void readJsonPartialString(String str, JsonStatus s) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (s.openSimpleQuotes) {
                if (s.openAntiSlash) {
                    s.openAntiSlash = false;
                } else if (c == '\'') {
                    s.openSimpleQuotes = false;
                }
            } else if (s.openDoubleQuotes) {
                if (s.openAntiSlash) {
                    s.openAntiSlash = false;
                } else if (c == '\"') {
                    s.openDoubleQuotes = false;
                }
            } else if (s.openAntiSlash) {
                s.openAntiSlash = false;
            } else {
                switch (c) {
                    case '\\': {
                        s.openAntiSlash = true;
                        break;
                    }
                    case '\'': {
                        s.openSimpleQuotes = true;
                        break;
                    }
                    case '\"': {
                        s.openDoubleQuotes = true;
                        break;
                    }
                    case '{': {
                        s.openBraces++;
                        s.countBraces++;
                        break;
                    }
                    case '}': {
                        s.openBraces--;
                        break;
                    }
                    case '[': {
                        s.openBrackets++;
                        break;
                    }
                    case ']': {
                        s.openBrackets--;
                        break;
                    }
                }
            }
        }
    }

    public static String[] getJsonObjectStringArray(JsonObject jsonObject, String param) {
        List<String> arch = new ArrayList<>();
        JsonArray archArr = JsonUtils.isNull(jsonObject.get(param)) ? null : jsonObject.getJsonArray(param);
        if (archArr != null) {
            for (JsonValue dependency : archArr) {
                arch.add((((JsonString) dependency).getString()));
            }
        }
        return arch.toArray(new String[arch.size()]);
    }

    public static class SerializeOptions {

        boolean pretty;
        boolean ignoreNulls;
        boolean ignoreEmptyStrings;
        boolean ignoreEmptyMaps;
        boolean ignoreEmptyCollections;
        boolean ignoreEmptyArrays;

        public boolean isPretty() {
            return pretty;
        }

        public SerializeOptions setPretty(boolean pretty) {
            this.pretty = pretty;
            return this;
        }

        public boolean isIgnoreNulls() {
            return ignoreNulls;
        }

        public SerializeOptions setIgnoreNulls(boolean ignoreNulls) {
            this.ignoreNulls = ignoreNulls;
            return this;
        }

        public boolean isIgnoreEmptyStrings() {
            return ignoreEmptyStrings;
        }

        public SerializeOptions setIgnoreEmptyStrings(boolean ignoreEmptyStrings) {
            this.ignoreEmptyStrings = ignoreEmptyStrings;
            return this;
        }

        public boolean isIgnoreEmptyMaps() {
            return ignoreEmptyMaps;
        }

        public SerializeOptions setIgnoreEmptyMaps(boolean ignoreEmptyMaps) {
            this.ignoreEmptyMaps = ignoreEmptyMaps;
            return this;
        }

        public boolean isIgnoreEmptyCollections() {
            return ignoreEmptyCollections;
        }

        public SerializeOptions setIgnoreEmptyCollections(boolean ignoreEmptyCollections) {
            this.ignoreEmptyCollections = ignoreEmptyCollections;
            return this;
        }

        public boolean isIgnoreEmptyArrays() {
            return ignoreEmptyArrays;
        }

        public SerializeOptions setIgnoreEmptyArrays(boolean ignoreEmptyArrays) {
            this.ignoreEmptyArrays = ignoreEmptyArrays;
            return this;
        }
    }

    public static class JsonStringBuffer {

        private StringBuilder sb = new StringBuilder();
        private JsonStatus status = new JsonStatus();

        public boolean append(String line) {
            JsonUtils.readJsonPartialString(line, status);
            status.checkPartialValid(true);
            sb.append(line);
            if (status.countBraces > 0 && status.checkValid(false)) {
                return true;
            }
            return true;
        }

        public String getValidString() {
            status.checkValid(true);
            return sb.toString();
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }

    public static class JsonStatus {

        public int countBraces;
        public int openBraces;
        public int openBrackets;
        public boolean openAntiSlash;
        public boolean openSimpleQuotes;
        public boolean openDoubleQuotes;

        boolean checkValid(boolean throwError) {
            if (!checkPartialValid(throwError)) {
                return false;
            }
            if (countBraces == 0) {
                if (throwError) {
                    throw new RuntimeException("not an object");
                }
                return false;
            }
            if (openBrackets > 0) {
                if (throwError) {
                    throw new RuntimeException("Unbalanced brackets");
                }
                return false;
            }
            if (openBraces > 0) {
                if (throwError) {
                    throw new RuntimeException("Unbalanced braces");
                }
                return false;
            }
            if (openAntiSlash) {
                if (throwError) {
                    throw new RuntimeException("Unbalanced anti-slash");
                }
            }
            if (openSimpleQuotes) {
                if (throwError) {
                    throw new RuntimeException("Unbalanced simple quotes");
                }
                return false;
            }
            if (openDoubleQuotes) {
                if (throwError) {
                    throw new RuntimeException("Unbalanced double quotes");
                }
                return false;
            }
            return true;
        }

        boolean checkPartialValid(boolean throwError) {
            if (openBrackets < 0) {
                if (throwError) {
                    throw new RuntimeException("Unbalanced brackets");
                }
                return false;
            }
            if (openBraces < 0) {
                if (throwError) {
                    throw new RuntimeException("Unbalanced braces");
                }
                return false;
            }
            return true;
        }
    }
}
