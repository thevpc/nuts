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
package net.thevpc.nuts.runtime.core.format.elem;

import java.io.File;
import net.thevpc.nuts.runtime.bundles.reflect.SimpleParametrizedType;
import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.xml.NutsElementFactoryXmlDocument;
import net.thevpc.nuts.runtime.core.format.xml.NutsElementFactoryXmlElement;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.bundles.collections.ClassMap;
import net.thevpc.nuts.runtime.bundles.reflect.ReflectProperty;
import net.thevpc.nuts.runtime.bundles.reflect.ReflectRepository;
import net.thevpc.nuts.runtime.bundles.reflect.ReflectType;
import net.thevpc.nuts.runtime.bundles.reflect.ReflectUtils;
import net.thevpc.nuts.runtime.core.model.DefaultNutsArtifactCall;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDefinition;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDependencyBuilder;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDescriptorBuilder;
import net.thevpc.nuts.runtime.standalone.DefaultNutsClassifierMapping;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsIdLocationBuilder;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

/**
 *
 * @author thevpc
 */
public class DefaultNutsElementFactoryService implements NutsElementFactoryService {

    private static final NutsElementMapper F_NULL = new NutsElementFactoryNull();
    private static final NutsElementMapper F_NUTS_ARR = new NutsElemenSerializationAdapterArr();
    private static final NutsElementMapper F_STRINGS = new NutsElementFactoryString();
    private static final NutsElementMapper F_CHAR = new NutsElementFactoryChar();
    private static final NutsElementMapper F_NUMBERS = new NutsElementFactoryNumber();
    private static final NutsElementMapper F_BOOLEANS = new NutsElementFactoryBoolean();
    private static final NutsElementMapper F_ENUMS = new NutsElementFactoryEnum();
    private static final NutsElementMapper F_INSTANT = new NutsElementFactoryInstant();
    private static final NutsElementMapper F_DATE = new NutsElementFactoryUtilDate();
    private static final NutsElementMapper F_PATH = new NutsElementFactoryPath();
    private static final NutsElementMapper F_FILE = new NutsElementFactoryFile();
    private static final NutsElementMapper F_ITERATOR = new NutsElementFactoryIterator();
    private static final NutsElementMapper F_NAMED_ELEM = new NutsElementFactoryNamedElement();
    private static final NutsElementMapper F_MAPENTRY = new NutsElementFactoryMapEntry();
    private static final NutsElementMapper F_XML_ELEMENT = new NutsElementFactoryXmlElement();
    private static final NutsElementMapper F_XML_DOCUMENT = new NutsElementFactoryXmlDocument();
    private static final NutsElementMapper F_NUTS_DEF = new NutsElementFactoryNutsDefinition();
    private static final NutsElementMapper F_NUTS_ID = new NutsElementFactoryNutsId();
    private static final NutsElementMapper F_NUTS_VERSION = new NutsElementFactoryNutsVersion();
    private static final NutsElementMapper F_NUTS_DESCRIPTOR = new NutsElementFactoryNutsDescriptor();
    private static final NutsElementMapper F_NUTS_DEPENDENCY = new NutsElementFactoryNutsDependency();
    private static final NutsElementMapper F_NUTS_SDK_LOCATION = new NutsElementFactoryNutsSdkLocation();
    private static final NutsElementMapper F_NUTS_ID_LOCATION = new NutsElementFactoryNutsIdLocation();
    private static final NutsElementMapper F_NUTS_CLASSIFIER_MAPPING = new NutsElementFactoryNutsClassifierMapping();
    private static final NutsElementMapper F_ARTIFACT_CALL = new NutsElementFactoryNutsArtifactCall();
//    public static final NutsElementFactory F_JSONELEMENT = new NutsElementFactoryJsonElement();

    private final ClassMap<NutsElementMapper> defaultFactories = new ClassMap<>(null, NutsElementMapper.class);
    private final ClassMap<NutsElementMapper> factories = new ClassMap<>(null, NutsElementMapper.class);
    private ReflectRepository typesRepository;
    private final NutsWorkspace ws;
    private final NutsElementMapper F_OBJ = new NutsElemenSerializationAdapterObjReflect();

    private final NutsElementMapper F_COLLECTION = new NutsElementFactoryCollection();
    private final NutsElementMapper F_MAP = new NutsElementFactoryMap();
    public DefaultNutsElementFactoryService(NutsWorkspace ws) {
        typesRepository = NutsWorkspaceUtils.of(ws).getReflectRepository();
        addDefaultFactory(Boolean.class, F_BOOLEANS);
        addDefaultFactory(boolean.class, F_BOOLEANS);
        addDefaultFactory(byte.class, F_NUMBERS);
        addDefaultFactory(short.class, F_NUMBERS);
        addDefaultFactory(int.class, F_NUMBERS);
        addDefaultFactory(long.class, F_NUMBERS);
        addDefaultFactory(float.class, F_NUMBERS);
        addDefaultFactory(double.class, F_NUMBERS);
        addDefaultFactory(Number.class, F_NUMBERS);

        addDefaultFactory(char.class, F_CHAR);
        addDefaultFactory(Character.class, F_CHAR);

        addDefaultFactory(Object.class, F_OBJ);
        addDefaultFactory(String.class, F_STRINGS);

        addDefaultFactory(StringBuilder.class, F_STRINGS);
        addDefaultFactory(StringBuffer.class, F_STRINGS);

        addDefaultFactory(Path.class, F_PATH);
        addDefaultFactory(File.class, F_FILE);
        addDefaultFactory(java.util.Date.class, F_DATE);
        addDefaultFactory(java.time.Instant.class, F_INSTANT);
        addDefaultFactory(Enum.class, F_ENUMS);
        addDefaultFactory(Collection.class, F_COLLECTION);
        addDefaultFactory(Iterator.class, F_ITERATOR);
        addDefaultFactory(Map.class, F_MAP);
        addDefaultFactory(Map.Entry.class, F_MAPENTRY);
        addDefaultFactory(org.w3c.dom.Element.class, F_XML_ELEMENT);
        addDefaultFactory(org.w3c.dom.Document.class, F_XML_DOCUMENT);
        addDefaultFactory(boolean[].class, new NutsElementFactoryPrimitiveBooleanArray());
        addDefaultFactory(byte[].class, new NutsElementFactoryPrimitiveByteArray());
        addDefaultFactory(short[].class, new NutsElementFactoryPrimitiveShortArray());
        addDefaultFactory(char[].class, new NutsElementFactoryPrimitiveCharArray());
        addDefaultFactory(int[].class, new NutsElementFactoryPrimitiveIntArray());
        addDefaultFactory(long[].class, new NutsElementFactoryPrimitiveLongArray());
        addDefaultFactory(float[].class, new NutsElementFactoryFloatArray());
        addDefaultFactory(double[].class, new NutsElementFactoryPrimitiveDoubleArray());
        addDefaultFactory(Object[].class, new NutsElementFactoryObjectArray());
        addDefaultFactory(NutsPrimitiveElement.class, new NutsElementFactoryNutsPrimitiveElement());
        addDefaultFactory(NutsArrayElement.class, new NutsElementFactoryNutsArrayElement());
        addDefaultFactory(NutsObjectElement.class, new NutsElementFactoryNutsObjectElement());
        addDefaultFactory(NutsElement.class, new NutsElementFactoryNutsElement());
        addDefaultFactory(NutsElementEntry.class, F_NAMED_ELEM);

//        addHierarchyFactory(JsonElement.class, F_JSONELEMENT);
        addCustomFactory(NutsDefinition.class, F_NUTS_DEF);
        addCustomFactory(NutsId.class, F_NUTS_ID);
        addCustomFactory(NutsVersion.class, F_NUTS_VERSION);
        addCustomFactory(NutsDescriptor.class, F_NUTS_DESCRIPTOR);
        addCustomFactory(NutsDependency.class, F_NUTS_DEPENDENCY);
        addCustomFactory(NutsIdLocation.class, F_NUTS_ID_LOCATION);
        addCustomFactory(NutsClassifierMapping.class, F_NUTS_CLASSIFIER_MAPPING);
        addCustomFactory(NutsArtifactCall.class, F_ARTIFACT_CALL);
        addCustomFactory(NutsSdkLocation.class, F_NUTS_SDK_LOCATION);
        this.ws = ws;
    }

    public final void addDefaultFactory(Class cls, NutsElementMapper instance) {
        defaultFactories.put(cls, instance);
    }

    public final void addCustomFactory(Class cls, NutsElementMapper instance) {
        factories.put(cls, instance);
    }

    public NutsElementMapper getElementFactory(Type type, boolean defaultOnly) {
        if (type == null) {
            return F_NULL;
        }
        Class cls = ReflectUtils.getRawClass(type);
        if (cls.isArray()) {
            NutsElementMapper f = defaultFactories.getExact(cls);
            if (f != null) {
                return f;
            }
            return F_NUTS_ARR;
        }
        if (!defaultOnly) {
            NutsElementMapper f = factories.get(cls);
            if (f != null) {
                return f;
            }
        }
        final NutsElementMapper r = defaultFactories.get(cls);
        if (r != null) {
            return r;
        }
        throw new IllegalArgumentException("Unable to find serialization factory for " + type);
    }

    @Override
    public Object createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        if (o==null || o.type() == NutsElementType.NULL) {
            return F_NULL.createObject(o, to, context);
        }
        NutsElementMapper f = getElementFactory(to, false);
        return f.createObject(o, to, context);
    }

    @Override
    public Object defaultCreateObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        if (o==null || o.type() == NutsElementType.NULL) {
            return F_NULL.createElement(null, to, context);
        }
        NutsElementMapper f = getElementFactory(to, true);
        return f.createObject(o, to, context);
    }

    @Override
    public NutsElement createElement(Object o, Type expectedType, NutsElementFactoryContext context) {
        if (o == null) {
            return context.element().forPrimitive().buildNull();
        }
        if (expectedType == null) {
            expectedType = o.getClass();
        }
        NutsElementMapper ff = getElementFactory(expectedType, false);
        if (ff == null) {
            ff = getElementFactory(expectedType, false);
        }
        return ff.createElement(o, expectedType, context);
    }

    @Override
    public NutsElement defaultCreateElement(Object o, Type expectedType, NutsElementFactoryContext context) {
        if (expectedType == null && o != null) {
            expectedType = o.getClass();
        }
        if (o == null) {
            return context.element().forPrimitive().buildNull();
        }
        return getElementFactory(expectedType, true).createElement(o, o.getClass(), context);
    }

    private static class NutsElementFactoryNamedElement implements NutsElementMapper<NutsElementEntry> {

        @Override
        public NutsElement createElement(NutsElementEntry o, Type typeOfSrc, NutsElementFactoryContext context) {
            NutsElementEntry je = (NutsElementEntry) o;
            Map<String, Object> m = new HashMap<>();
            m.put("key", je.getKey());
            m.put("value", je.getValue());
            return context.objectToElement(m, ReflectUtils.createParametrizedType(Map.class, String.class, Object.class));
        }

        @Override
        public NutsElementEntry createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            Type[] args = (typeOfResult instanceof ParameterizedType)
                    ? (((ParameterizedType) typeOfResult).getActualTypeArguments())
                    : new Type[]{Object.class, Object.class};
            Type mapType = new SimpleParametrizedType(Map.class, args);
            Map map = (Map) context.elementToObject(o, mapType);
            return new DefaultNutsElementEntry(
                    (NutsElement) map.get("key"),
                    (NutsElement) map.get("value")
            );
        }

    }

    private class NutsElementFactoryMap implements NutsElementMapper<Map> {

        @Override
        public NutsElement createElement(Map o, Type typeOfSrc, NutsElementFactoryContext context) {
            Map je = (Map) o;
            Map<NutsElement, NutsElement> m = new LinkedHashMap<>();
            if (je != null) {
                for (Object e0 : je.entrySet()) {
                    Map.Entry e = (Map.Entry) e0;
                    NutsElement k = context.defaultObjectToElement(e.getKey(), null);
                    NutsElement v = context.defaultObjectToElement(e.getValue(), null);
                    m.put(k, v);
                }
            }
            return new DefaultNutsObjectElement(m, context.getWorkspace());
        }

        public Map fillObject(NutsElement o, Map all, Type elemType1, Type elemType2, Type to, NutsElementFactoryContext context) {
            if (o.type() == NutsElementType.OBJECT) {
                for (NutsElementEntry kv : o.asObject().children()) {
                    NutsElement k = kv.getKey();
                    NutsElement v = kv.getValue();
                    all.put(context.elementToObject(k, elemType1), context.elementToObject(v, elemType2));
                }
            } else if (o.type() == NutsElementType.ARRAY) {
                for (NutsElement ee : o.asArray().children()) {
                    NutsObjectElement kv = ee.asObject();
                    NutsElement k = kv.get("key");
                    NutsElement v = kv.get("value");
                    all.put(context.elementToObject(k, elemType1), context.elementToObject(v, elemType2));
                }
            } else {
                throw new IllegalArgumentException("unsupported");
            }

            return all;
        }

        @Override
        public Map createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            Class cls = Map.class;
            Type elemType1 = null;//Object.class;
            Type elemType2 = null;//Object.class;
            if (to instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) to;
                Type rawType = pt.getRawType();
                if (rawType instanceof Class) {
                    cls = (Class) rawType;
                }
                elemType1 = pt.getActualTypeArguments()[0];
                elemType2 = pt.getActualTypeArguments()[1];
            }
            if (cls == null) {
                throw new IllegalArgumentException("invalid");
            }
            switch (cls.getName()) {
                case "java.util.Map":
                case "java.util.LinkedHashMap": {
                    return fillObject(o, new LinkedHashMap(o.asObject().size()), elemType1, elemType2, to, context);
                }
                case "java.util.HashMap": {
                    return fillObject(o, new HashMap(o.asObject().size()), elemType1, elemType2, to, context);
                }
                case "java.util.SortedMap":
                case "java.util.NavigableMap": {
                    return fillObject(o, new TreeMap(), elemType1, elemType2, to, context);
                }
                default: {
                    return fillObject(o, (Map) typesRepository.getType(to).newInstance(), elemType1, elemType2, to, context);
                }
            }
        }

    }

    private static class NutsElementFactoryMapEntry implements NutsElementMapper<Map.Entry> {

        @Override
        public NutsElement createElement(Map.Entry o, Type typeOfSrc, NutsElementFactoryContext context) {
            Map.Entry je = (Map.Entry) o;
            return context.element().forObject()
                    .set("key", context.objectToElement(je.getKey(), null))
                    .set("value", context.objectToElement(je.getValue(), null))
                    .build();
        }

        @Override
        public Map.Entry createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            if (to instanceof ParameterizedType) {
                Type[] kvt = ((ParameterizedType) to).getActualTypeArguments();
                return new AbstractMap.SimpleEntry(
                        context.elementToObject(o.asObject().get("key"), kvt[0]),
                        context.elementToObject(o.asObject().get("value"), kvt[0])
                );
            }
            return new AbstractMap.SimpleEntry(
                    context.elementToObject(o.asObject().get("key"), Object.class),
                    context.elementToObject(o.asObject().get("value"), Object.class)
            );
        }

    }

    private class NutsElementFactoryCollection implements NutsElementMapper {

        @Override
        public NutsElement createElement(Object o, Type typeOfSrc, NutsElementFactoryContext context) {
            Collection<Object> coll = (Collection) o;
            List<NutsElement> collect = coll.stream().map(x -> context.objectToElement(x, null)).collect(Collectors.toList());
            return new DefaultNutsArrayElement(collect);
        }

        public Collection fillObject(NutsElement o, Collection coll, Type elemType, Type to, NutsElementFactoryContext context) {
            for (NutsElement nutsElement : o.asArray().children()) {
                coll.add(context.elementToObject(nutsElement, elemType));
            }
            return coll;
        }

        @Override
        public Collection createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            Class cls = ReflectUtils.getRawClass(to);
            Type elemType = Object.class;
            if (to instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) to;
                elemType = pt.getActualTypeArguments()[0];
            }
//            if (cls == null) {
//                throw new IllegalArgumentException("invalid");
//            }
            switch (cls.getName()) {
                case "java.util.Collection":
                case "java.util.List":
                case "java.util.ArrayList": {
                    return fillObject(o, new ArrayList(o.asArray().size()), elemType, to, context);
                }
                case "java.util.Set":
                case "java.util.LinkedHashset": {
                    return fillObject(o, new LinkedHashSet(), elemType, to, context);
                }
                case "java.util.Hashset": {
                    return fillObject(o, new HashSet(), elemType, to, context);
                }
                case "java.util.SortedSet":
                case "java.util.NavigableSet":
                case "java.util.TreeSet": {
                    return fillObject(o, new TreeSet(), elemType, to, context);
                }
                case "java.util.Queue": {
                    return fillObject(o, new LinkedList(), elemType, to, context);
                }
                case "java.util.BlockingQueue": {
                    return fillObject(o, new LinkedBlockingQueue(), elemType, to, context);
                }
                case "java.util.TransferQueue": {
                    return fillObject(o, new LinkedTransferQueue(), elemType, to, context);
                }
                case "java.util.Deque": {
                    return fillObject(o, new ArrayList(), elemType, to, context);
                }
                default: {
                    ReflectType m = typesRepository.getType(to);
                    return fillObject(o, (Collection) m.newInstance(), elemType, to, context);
                }
            }
        }

    }

    private static class NutsElementFactoryIterator implements NutsElementMapper<Iterator> {

        @Override
        public NutsElement createElement(Iterator o, Type typeOfSrc, NutsElementFactoryContext context) {
            Iterator nl = (Iterator) o;
            List<NutsElement> values = new ArrayList<>();
            while (nl.hasNext()) {
                values.add(context.objectToElement(nl.next(), null));
            }
            return new DefaultNutsArrayElement(values);
        }

        @Override
        public Iterator createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            Class elemType = Object.class;
            return o.asArray().children().stream().map(x -> context.elementToObject(x, elemType)).collect(
                    Collectors.toList()
            ).iterator();
        }

    }

    private static class NutsElementFactoryInstant implements NutsElementMapper<Instant> {

        @Override
        public NutsElement createElement(Instant o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.element().forPrimitive().buildInstant((Instant) o);
        }

        @Override
        public Instant createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            switch (o.type()) {
                case INSTANT: {
                    return o.asPrimitive().getInstant();
                }
                case INTEGER: {
                    return Instant.ofEpochMilli(o.asPrimitive().getInt());
                }
                case LONG: {
                    return Instant.ofEpochMilli(o.asPrimitive().getLong());
                }
                case STRING: {
                    return Instant.parse(o.asPrimitive().getString());
                }
            }
            throw new IllegalArgumentException("unable to parse instant " + o);
        }
    }

    private static class NutsElementFactoryUtilDate implements NutsElementMapper<java.util.Date> {

        @Override
        public NutsElement createElement(java.util.Date o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.element().forPrimitive().buildInstant(o.toInstant());
        }

        @Override
        public java.util.Date createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            Instant i = (Instant) context.defaultElementToObject(o, Instant.class);
            return new Date(i.toEpochMilli());
        }
    }

    private static class NutsElementFactoryNumber implements NutsElementMapper<Number> {

        @Override
        public NutsElement createElement(Number o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.element().forPrimitive().buildNumber((Number) o);
        }

        @Override
        public Number createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            switch (((Class) to).getName()) {
                case "byte":
                case "java.lang.Byte":
                    return o.asPrimitive().getByte();
                case "short":
                case "java.lang.Short":
                    return o.asPrimitive().getShort();
                case "int":
                case "java.lang.Integer":
                    return o.asPrimitive().getInt();
                case "long":
                case "java.lang.Long":
                    return o.asPrimitive().getShort();
                case "float":
                case "java.lang.Float":
                    return o.asPrimitive().getShort();
                case "double":
                case "java.lang.Double":
                    return o.asPrimitive().getShort();
                case "java.lang.BigDecimal":
                    return new BigDecimal(o.asPrimitive().getString());
                case "java.lang.BigInteger":
                    return new BigInteger(o.asPrimitive().getString());
            }
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private static class NutsElementFactoryBoolean implements NutsElementMapper<Boolean> {

        @Override
        public NutsElement createElement(Boolean o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.element().forPrimitive().buildBoolean((Boolean) o);
        }

        @Override
        public Boolean createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            switch (((Class) to).getName()) {
                case "boolean":
                case "java.lang.Boolean":
                    return o.asPrimitive().getBoolean();
            }
            throw new UnsupportedOperationException("Not supported.");
        }

    }

    private static class NutsElementFactoryEnum implements NutsElementMapper<Enum> {

        @Override
        public NutsElement createElement(Enum o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.element().forPrimitive().buildString(String.valueOf(o));
        }

        @Override
        public Enum createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            switch (o.type()) {
                case BYTE:
                case SHORT:
                case INTEGER:
                case LONG: {
                    NutsPrimitiveElement p = o.asPrimitive();
                    return (Enum) ((Class) to).getEnumConstants()[p.getInt()];
                }
                case STRING: {
                    NutsPrimitiveElement p = o.asPrimitive();
                    return Enum.valueOf(ReflectUtils.getRawClass(to), p.getString());
                }
            }
            throw new IllegalArgumentException("unexpected");
        }
    }

    private static class NutsElementFactoryChar implements NutsElementMapper<Character> {

        @Override
        public NutsElement createElement(Character o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.element().forPrimitive().buildString(String.valueOf(o));
        }

        @Override
        public Character createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            final String s = o.asPrimitive().getString();
            return (s == null || s.isEmpty())
                    ? (((to instanceof Class) && ((Class) to).isPrimitive()) ? '\0' : null)
                    : s.charAt(0);
        }
    }

    private static class NutsElementFactoryString implements NutsElementMapper<String> {

        @Override
        public NutsElement createElement(String o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.element().forPrimitive().buildString(String.valueOf(o));
        }

        @Override
        public String createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            return o.asPrimitive().getString();
        }
    }

    private static class NutsElementFactoryNull implements NutsElementMapper<Object> {

        @Override
        public NutsElement createElement(Object o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.element().forPrimitive().buildNull();
        }

        @Override
        public Object createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            if (to instanceof Class) {
                Class c = (Class) to;
                if (c.isPrimitive()) {
                    switch (c.getName()) {
                        case "boolean":
                            return false;
                        case "byte":
                            return (byte) 0;
                        case "short":
                            return (short) 0;
                        case "int":
                            return 0;
                        case "char":
                            return (char) 0;
                        case "long":
                            return (long) 0;
                        case "float":
                            return (float) 0;
                        case "double":
                            return (double) 0;
                    }
                }
            }
            return null;
        }

    }

    private static class NutsElementFactoryNutsDefinition implements NutsElementMapper<NutsDefinition> {

        @Override
        public NutsElement createElement(NutsDefinition o, Type typeOfSrc, NutsElementFactoryContext context) {
            DefaultNutsDefinition dd = (o instanceof DefaultNutsDefinition) ? (DefaultNutsDefinition) o : new DefaultNutsDefinition(o, context.getWorkspace());
            return context.defaultObjectToElement(dd, null);
        }

        @Override
        public NutsDefinition createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            return (NutsDefinition) context.defaultElementToObject(o, DefaultNutsDefinition.class);
        }
    }

    private static class NutsElementFactoryNutsClassifierMapping implements NutsElementMapper<NutsClassifierMapping> {

        @Override
        public NutsElement createElement(NutsClassifierMapping o, Type typeOfSrc, NutsElementFactoryContext context) {
            DefaultNutsClassifierMapping dd = (o instanceof DefaultNutsClassifierMapping) ? (DefaultNutsClassifierMapping) o : new DefaultNutsClassifierMapping(o);
            return context.defaultObjectToElement(dd, null);
        }

        @Override
        public NutsClassifierMapping createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            return (NutsClassifierMapping) context.defaultElementToObject(o, DefaultNutsClassifierMapping.class);
        }
    }

    private static class NutsElementFactoryNutsArtifactCall implements NutsElementMapper<NutsArtifactCall> {

        @Override
        public NutsElement createElement(NutsArtifactCall o, Type typeOfSrc, NutsElementFactoryContext context) {
            DefaultNutsArtifactCall dd = (o instanceof DefaultNutsArtifactCall) ? (DefaultNutsArtifactCall) o : new DefaultNutsArtifactCall(o);
            return context.defaultObjectToElement(dd, null);
        }

        @Override
        public NutsArtifactCall createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsObjectElement object = o.asObject();
            NutsId id = (NutsId) context.elementToObject(object.get("id"), NutsId.class);
            String[] arguments = (String[]) context.elementToObject(object.get("arguments"), String[].class);
            Map<String, String> properties = (Map<String, String>) context.elementToObject(object.get("properties"), ReflectUtils.createParametrizedType(Map.class, String.class, String.class));

            return new DefaultNutsArtifactCall(id, arguments, properties);
        }
    }

    private static class NutsElementFactoryNutsId implements NutsElementMapper<NutsId> {

        @Override
        public NutsElement createElement(NutsId o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.defaultObjectToElement(o.toString(), null);
        }

        @Override
        public NutsId createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            return context.getWorkspace().id().parser().parse(o.asPrimitive().getString());
        }

    }

    private static class NutsElementFactoryNutsVersion implements NutsElementMapper<NutsVersion> {

        @Override
        public NutsElement createElement(NutsVersion o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.defaultObjectToElement(o.toString(), null);
        }

        @Override
        public NutsVersion createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            return context.getWorkspace().version().parser().parse(o.asPrimitive().getString());
        }

    }

    private static class NutsElementFactoryPath implements NutsElementMapper<Path> {

        @Override
        public NutsElement createElement(Path o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.defaultObjectToElement(o.toString(), null);
        }

        @Override
        public Path createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            return Paths.get(o.asPrimitive().getString());
        }
    }

    private static class NutsElementFactoryFile implements NutsElementMapper<File> {

        @Override
        public NutsElement createElement(File o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.defaultObjectToElement(o.toString(), null);
        }

        @Override
        public File createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            return new File(o.asPrimitive().getString());
        }
    }

    private static class NutsElementFactoryNutsDescriptor implements NutsElementMapper<NutsDescriptor> {

        @Override
        public NutsElement createElement(NutsDescriptor o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.defaultObjectToElement(context.getWorkspace().descriptor().descriptorBuilder().set(o), null
            );
        }

        @Override
        public NutsDescriptor createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            DefaultNutsDescriptorBuilder builder = (DefaultNutsDescriptorBuilder) context.defaultElementToObject(o, DefaultNutsDescriptorBuilder.class);
            return context.getWorkspace().descriptor().descriptorBuilder().set(builder).build();
        }

    }

    private static class NutsElementFactoryNutsSdkLocation implements NutsElementMapper<NutsSdkLocation> {

        @Override
        public NutsElement createElement(NutsSdkLocation o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.defaultObjectToElement(o, null);
        }

        @Override
        public NutsSdkLocation createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsObjectElement obj = o.asObject();
            NutsId id = context.elementToObject(obj.get("id"), NutsId.class);
            String product = context.elementToObject(obj.get("product"), String.class);
            String name = context.elementToObject(obj.get("name"), String.class);
            String path = context.elementToObject(obj.get("path"), String.class);
            String version = context.elementToObject(obj.get("version"), String.class);
            String packaging = context.elementToObject(obj.get("packaging"), String.class);
            int priority = context.elementToObject(obj.get("priority"), int.class);
            return new NutsSdkLocation(id, product, name, path, version, packaging, priority);
        }

    }

    private static class NutsElementFactoryNutsDependency implements NutsElementMapper<NutsDependency> {

        @Override
        public NutsElement createElement(NutsDependency o, Type typeOfSrc, NutsElementFactoryContext context) {
            if (o.getExclusions().length == 0) {
                //use compact form
                return context.defaultObjectToElement(context.getWorkspace().dependency().formatter(o)
                        .setNtf(false)
                        .format(), null);
            }
            return context.defaultObjectToElement(context.getWorkspace().dependency().builder().set(o), null);
        }

        @Override
        public NutsDependency createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            if (o.type() == NutsElementType.STRING) {
                return context.getWorkspace().dependency().parser().setLenient(false).parseDependency(o.asPrimitive().getString());
            }
            DefaultNutsDependencyBuilder builder = (DefaultNutsDependencyBuilder) context.defaultElementToObject(o, DefaultNutsDependencyBuilder.class);
            return context.getWorkspace().dependency().builder().set(builder).build();
        }

    }

    private static class NutsElementFactoryNutsIdLocation implements NutsElementMapper<NutsIdLocation> {

        @Override
        public NutsElement createElement(NutsIdLocation o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.defaultObjectToElement(new DefaultNutsIdLocationBuilder().set(o), null
            );
        }

        @Override
        public NutsIdLocation createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            DefaultNutsIdLocationBuilder builder = (DefaultNutsIdLocationBuilder) context.defaultElementToObject(o, DefaultNutsIdLocationBuilder.class);
            return builder.build();
        }

    }

    private class NutsElemenSerializationAdapterObjReflect implements NutsElementMapper<Object> {

        @Override
        public NutsElement createElement(Object src, Type typeOfSrc, NutsElementFactoryContext context) {
            ReflectType m = typesRepository.getType(typeOfSrc);
            NutsObjectElementBuilder obj = context.element().forObject();
            for (ReflectProperty property : m.getProperties()) {
                final Object v = property.read(src);
                if (!property.isDefaultValue(v)) {
                    obj.set(property.getName(), context.objectToElement(v, null));
                }
            }
            return obj.build();
        }

        @Override
        public Object createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            Class c = ReflectUtils.getRawClass(typeOfResult);
            int mod = c.getModifiers();
            if (Modifier.isAbstract(mod)) {
                throw new IllegalArgumentException("cannot instantate abstract class " + typeOfResult);
            }
            ReflectType m = typesRepository.getType(typeOfResult);
            Object instance = m.newInstance();
            NutsObjectElement eobj = o.asObject();
            for (ReflectProperty property : m.getProperties()) {
                if (property.isWrite()) {
                    NutsElement v = eobj.get(property.getName());
                    if (v != null) {
                        property.write(instance, context.elementToObject(v, property.getPropertyType()));
                    }
                }
            }
            return instance;
        }

    }

    private static class NutsElemenSerializationAdapterArr implements NutsElementMapper<Object> {

        public NutsElemenSerializationAdapterArr() {
        }

        @Override
        public Object createObject(NutsElement json, Type typeOfResult, NutsElementFactoryContext context) {
            NutsArrayElement e = (NutsArrayElement) json;
            Class arrType = (Class) typeOfResult;
            Class componentType = arrType.getComponentType();
            switch (componentType.getName()) {
                case "boolean": {
                    boolean[] x = new boolean[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).asPrimitive().getBoolean();
                    }
                    return x;
                }
                case "byte": {
                    byte[] x = new byte[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).asPrimitive().getByte();
                    }
                    return x;
                }
                case "short": {
                    short[] x = new short[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).asPrimitive().getShort();
                    }
                    return x;
                }
                case "int": {
                    int[] x = new int[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).asPrimitive().getInt();
                    }
                    return x;
                }
                case "long": {
                    long[] x = new long[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).asPrimitive().getLong();
                    }
                    return x;
                }
                case "float": {
                    float[] x = new float[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).asPrimitive().getFloat();
                    }
                    return x;
                }
                case "double": {
                    double[] x = new double[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).asPrimitive().getDouble();
                    }
                    return x;
                }
                default: {
                    Object[] x = (Object[]) Array.newInstance(componentType, e.size());
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = context.elementToObject(e.get(i), componentType);
                    }
                    return x;
                }

            }
        }

        public NutsElement createElement(Object src, Type typeOfSrc, NutsElementFactoryContext context) {
            return _createArray1(src, context);
        }

    }

    private static class NutsElementFactoryPrimitiveBooleanArray implements NutsElementMapper<boolean[]> {

        public NutsElementFactoryPrimitiveBooleanArray() {
        }

        @Override
        public NutsElement createElement(boolean[] src, Type typeOfSrc, NutsElementFactoryContext context) {
            return _createArray1(src, context);
        }

        @Override
        public boolean[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsArrayElement earr = o.asArray();
            boolean[] arr = new boolean[earr.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (boolean) context.elementToObject(earr.get(i), boolean.class);
            }
            return arr;
        }
    }

    private static class NutsElementFactoryPrimitiveByteArray implements NutsElementMapper<byte[]> {

        public NutsElementFactoryPrimitiveByteArray() {
        }

        @Override
        public NutsElement createElement(byte[] src, Type typeOfSrc, NutsElementFactoryContext context) {
            return _createArray1(src, context);
        }

        @Override
        public byte[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsArrayElement earr = o.asArray();
            byte[] arr = new byte[earr.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (byte) context.elementToObject(earr.get(i), byte.class);
            }
            return arr;
        }
    }

    private static class NutsElementFactoryPrimitiveShortArray implements NutsElementMapper<short[]> {

        public NutsElementFactoryPrimitiveShortArray() {
        }

        @Override
        public NutsElement createElement(short[] src, Type typeOfSrc, NutsElementFactoryContext context) {
            return _createArray1(src, context);
        }

        @Override
        public short[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsArrayElement earr = o.asArray();
            short[] arr = new short[earr.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (short) context.elementToObject(earr.get(i), short.class);
            }
            return arr;
        }
    }

    private static class NutsElementFactoryPrimitiveCharArray implements NutsElementMapper<char[]> {

        public NutsElementFactoryPrimitiveCharArray() {
        }

        @Override
        public NutsElement createElement(char[] src, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.element().forPrimitive().buildString(new String(src));
        }

        @Override
        public char[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsArrayElement earr = o.asArray();
            String s = (String) context.elementToObject(o, String.class);
            return s.toCharArray();
        }
    }

    private static class NutsElementFactoryPrimitiveIntArray implements NutsElementMapper<int[]> {

        public NutsElementFactoryPrimitiveIntArray() {
        }

        @Override
        public NutsElement createElement(int[] src, Type typeOfSrc, NutsElementFactoryContext context) {
            return _createArray1(src, context);
        }

        @Override
        public int[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsArrayElement earr = o.asArray();
            int[] arr = new int[earr.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (int) context.elementToObject(earr.get(i), int.class);
            }
            return arr;
        }
    }

    private static class NutsElementFactoryPrimitiveLongArray implements NutsElementMapper<long[]> {

        public NutsElementFactoryPrimitiveLongArray() {
        }

        @Override
        public NutsElement createElement(long[] src, Type typeOfSrc, NutsElementFactoryContext context) {
            return _createArray1(src, context);
        }

        @Override
        public long[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsArrayElement earr = o.asArray();
            long[] arr = new long[earr.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (long) context.elementToObject(earr.get(i), long.class);
            }
            return arr;
        }
    }

    private static class NutsElementFactoryFloatArray implements NutsElementMapper<float[]> {

        public NutsElementFactoryFloatArray() {
        }

        @Override
        public NutsElement createElement(float[] src, Type typeOfSrc, NutsElementFactoryContext context) {
            return _createArray1(src, context);
        }

        @Override
        public float[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsArrayElement earr = o.asArray();
            float[] arr = new float[earr.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (float) context.elementToObject(earr.get(i), float.class);
            }
            return arr;
        }
    }

    private static class NutsElementFactoryPrimitiveDoubleArray implements NutsElementMapper<double[]> {

        public NutsElementFactoryPrimitiveDoubleArray() {
        }

        @Override
        public NutsElement createElement(double[] src, Type typeOfSrc, NutsElementFactoryContext context) {
            return _createArray1(src, context);
        }

        @Override
        public double[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsArrayElement earr = o.asArray();
            double[] arr = new double[earr.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (double) context.elementToObject(earr.get(i), double.class);
            }
            return arr;
        }
    }

    private static class NutsElementFactoryObjectArray implements NutsElementMapper<Object[]> {

        public NutsElementFactoryObjectArray() {
        }

        @Override
        public NutsElement createElement(Object[] src, Type typeOfSrc, NutsElementFactoryContext context) {
            return _createArray1(src, context);
        }

        @Override
        public Object[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsArrayElement earr = o.asArray();
            Object[] arr = new Object[earr.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (Object) context.elementToObject(earr.get(i), Object.class);
            }
            return arr;
        }
    }

    private static class NutsElementFactoryNutsPrimitiveElement implements NutsElementMapper<NutsPrimitiveElement> {

        public NutsElementFactoryNutsPrimitiveElement() {
        }

        @Override
        public NutsElement createElement(NutsPrimitiveElement src, Type typeOfSrc, NutsElementFactoryContext context) {
            return src;
        }

        @Override
        public NutsPrimitiveElement createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            if (o.type().isPrimitive()) {
                return o.asPrimitive();
            }
            return context.element().forPrimitive().buildString(o.toString());
        }
    }

    private static class NutsElementFactoryNutsArrayElement implements NutsElementMapper<NutsArrayElement> {

        public NutsElementFactoryNutsArrayElement() {
        }

        @Override
        public NutsElement createElement(NutsArrayElement src, Type typeOfSrc, NutsElementFactoryContext context) {
            return src;
        }

        @Override
        public NutsArrayElement createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            if (o.type() == NutsElementType.ARRAY) {
                return o.asArray();
            }
            return context.element().forArray().add(o).build();
        }
    }

    private static class NutsElementFactoryNutsObjectElement implements NutsElementMapper<NutsObjectElement> {

        public NutsElementFactoryNutsObjectElement() {
        }

        @Override
        public NutsElement createElement(NutsObjectElement src, Type typeOfSrc, NutsElementFactoryContext context) {
            return src;
        }

        @Override
        public NutsObjectElement createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            if (o.type() == NutsElementType.OBJECT) {
                return o.asObject();
            }
            return context.element().forObject().set("value", o).build();
        }
    }

    private static class NutsElementFactoryNutsElement implements NutsElementMapper<NutsElement> {

        public NutsElementFactoryNutsElement() {
        }

        @Override
        public NutsElement createElement(NutsElement src, Type typeOfSrc, NutsElementFactoryContext context) {
            return src;
        }

        @Override
        public NutsElement createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            return o;
        }
    }

    private static NutsArrayElement _createArray1(Object array, NutsElementFactoryContext context) {
        if (array.getClass().getComponentType().isPrimitive()) {
            List<NutsElement> preloaded = new ArrayList<>();
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                preloaded.add(context.objectToElement(Array.get(array, i), null));
            }
            return new DefaultNutsArrayElement(preloaded);
        } else {
            return new DefaultNutsArrayElement(
                    Arrays.stream((Object[]) array).map(x -> context.objectToElement(x, null)).collect(Collectors.toList())
            );
        }
    }

}
