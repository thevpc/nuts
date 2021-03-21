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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private static final NutsElementFactory F_NULL = new NutsElementFactoryNull();
    private static final NutsElementFactory F_NUTS_ARR = new NutsElemenSerializationAdapterArr();
    private static final NutsElementFactory F_STRINGS = new NutsElementFactoryString();
    private static final NutsElementFactory F_CHAR = new NutsElementFactoryChar();
    private static final NutsElementFactory F_NUMBERS = new NutsElementFactoryNumber();
    private static final NutsElementFactory F_BOOLEANS = new NutsElementFactoryBoolean();
    private static final NutsElementFactory F_ENUMS = new NutsElementFactoryEnum();
    private static final NutsElementFactory F_INSTANT = new NutsElementFactoryInstant();
    private static final NutsElementFactory F_DATE = new NutsElementFactoryUtilDate();
    private static final NutsElementFactory F_PATH = new NutsElementFactoryPath();
    private static final NutsElementFactory F_FILE = new NutsElementFactoryFile();
    private static final NutsElementFactory F_COLLECTION = new NutsElementFactoryCollection();
    private static final NutsElementFactory F_ITERATOR = new NutsElementFactoryIterator();
    private static final NutsElementFactory F_MAP = new NutsElementFactoryMap();
    private static final NutsElementFactory F_NAMED_ELEM = new NutsElementFactoryNamedElement();
    private static final NutsElementFactory F_MAPENTRY = new NutsElementFactoryMapEntry();
    private static final NutsElementFactory F_XML_ELEMENT = new NutsElementFactoryXmlElement();
    private static final NutsElementFactory F_XML_DOCUMENT = new NutsElementFactoryXmlDocument();
    private static final NutsElementFactory F_NUTS_DEF = new NutsElementFactoryNutsDefinition();
    private static final NutsElementFactory F_NUTS_ID = new NutsElementFactoryNutsId();
    private static final NutsElementFactory F_NUTS_VERSION = new NutsElementFactoryNutsVersion();
    private static final NutsElementFactory F_NUTS_DESCRIPTOR = new NutsElementFactoryNutsDescriptor();
    private static final NutsElementFactory F_NUTS_DEPENDENCY = new NutsElementFactoryNutsDependency();
    private static final NutsElementFactory F_NUTS_SDK_LOCATION = new NutsElementFactoryNutsSdkLocation();
    private static final NutsElementFactory F_NUTS_ID_LOCATION = new NutsElementFactoryNutsIdLocation();
    private static final NutsElementFactory F_NUTS_CLASSIFIER_MAPPING = new NutsElementFactoryNutsClassifierMapping();
    private static final NutsElementFactory F_ARTIFACT_CALL = new NutsElementFactoryNutsArtifactCall();
//    public static final NutsElementFactory F_JSONELEMENT = new NutsElementFactoryJsonElement();

    private final ClassMap<NutsElementFactory> defaultFactories = new ClassMap<>(null, NutsElementFactory.class);
    private final ClassMap<NutsElementFactory> factories = new ClassMap<>(null, NutsElementFactory.class);
    private ReflectRepository typesRepository;
    private final NutsWorkspace ws;
    private final NutsElementFactory F_OBJ = new NutsElemenSerializationAdapterObjReflect();

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
        addDefaultFactory(NutsNamedElement.class, F_NAMED_ELEM);

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

    public final void addDefaultFactory(Class cls, NutsElementFactory instance) {
        defaultFactories.put(cls, instance);
    }

    public final void addCustomFactory(Class cls, NutsElementFactory instance) {
        factories.put(cls, instance);
    }

    public NutsElementFactory getElementFactory(Type type, boolean defaultOnly) {
        if (type == null) {
            return F_NULL;
        }
        Class cls = ReflectUtils.getRawClass(type);
        if (cls.isArray()) {
            NutsElementFactory f = defaultFactories.getExact(cls);
            if (f != null) {
                return f;
            }
            return F_NUTS_ARR;
        }
        if (!defaultOnly) {
            NutsElementFactory f = factories.get(cls);
            if (f != null) {
                return f;
            }
        }
        final NutsElementFactory r = defaultFactories.get(cls);
        if(r!=null){
            return r;
        }
        throw new IllegalArgumentException("Unable to find serialization factory for "+type);
    }

    @Override
    public Object createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        if (o.type() == NutsElementType.NULL) {
            return F_NULL.createObject(o, to, context);
        }
        NutsElementFactory f = getElementFactory(to, false);
        return f.createObject(o, to, context);
    }

    @Override
    public Object defaultCreateObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        if (o.type() == NutsElementType.NULL) {
            return F_NULL.createElement(null, to, context);
        }
        NutsElementFactory f = getElementFactory(to, true);
        return f.createObject(o, to, context);
    }

    @Override
    public NutsElement createElement(Object o, Type expectedType, NutsElementFactoryContext context) {
        if (o == null) {
            return context.elements().forNull();
        }
        if (expectedType == null) {
            expectedType = o.getClass();
        }
        NutsElementFactory ff = getElementFactory(expectedType, false);
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
            return context.elements().forNull();
        }
        return getElementFactory(expectedType, true).createElement(o, o.getClass(), context);
    }

    private static class NutsElementFactoryNamedElement implements NutsElementFactory<NutsNamedElement> {

        @Override
        public NutsElement createElement(NutsNamedElement o, Type typeOfSrc, NutsElementFactoryContext context) {
            NutsNamedElement je = (NutsNamedElement) o;
            Map<String, Object> m = new HashMap<>();
            m.put("name", je.getName());
            m.put("value", je.getValue());
            return context.objectToElement(m, ReflectUtils.createParametrizedType(Map.class, String.class, Object.class));
        }

        @Override
        public NutsNamedElement createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            Type[] args = (typeOfResult instanceof ParameterizedType)
                    ? (((ParameterizedType) typeOfResult).getActualTypeArguments())
                    : new Type[]{Object.class, Object.class};
            Type mapType = new SimpleParametrizedType(Map.class, args);
            Map map = (Map) context.elementToObject(o, mapType);
            return new DefaultNutsNamedElement(
                    (String) map.get("name"),
                    (NutsElement) map.get("value")
            );
        }

    }

    private static class NutsElementFactoryMap implements NutsElementFactory<Map> {

        @Override
        public NutsElement createElement(Map o, Type typeOfSrc, NutsElementFactoryContext context) {
            Map je = (Map) o;
            boolean type1 = true;
            for (Object object : je.keySet()) {
                if (!(object instanceof String)) {
                    type1 = false;
                    break;
                }
            }
            if (type1) {
                return new NutsObjectElementMap1(je, context);
            }
            return new NutsObjectElementMap2(je, context);
        }

        public Map fillObject(NutsElement o, Map all, Type elemType1, Type elemType2, Type to, NutsElementFactoryContext context) {
            if (o.type() == NutsElementType.OBJECT) {
                for (NutsNamedElement kv : o.object().children()) {
                    NutsElement k = context.elements().forString(kv.getName());
                    NutsElement v = kv.getValue();
                    all.put(context.elementToObject(k, elemType1), context.elementToObject(v, elemType2));
                }
            } else if (o.type() == NutsElementType.ARRAY) {
                for (NutsElement ee : o.array().children()) {
                    NutsObjectElement kv = ee.object();
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
                    return fillObject(o, new LinkedHashMap(o.object().size()), elemType1, elemType2, to, context);
                }
                case "java.util.HashMap": {
                    return fillObject(o, new HashMap(o.object().size()), elemType1, elemType2, to, context);
                }
            }
            throw new IllegalArgumentException("fix me");
        }

    }

    private static class NutsElementFactoryMapEntry implements NutsElementFactory<Map.Entry> {

        @Override
        public NutsElement createElement(Map.Entry o, Type typeOfSrc, NutsElementFactoryContext context) {
            Map.Entry je = (Map.Entry) o;
            Map<String, Object> m = new HashMap<>();
            m.put("key", je.getKey());
            m.put("value", je.getValue());
            return new NutsObjectElementMap1(m, context);
        }

        @Override
        public Map.Entry createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            if (to instanceof ParameterizedType) {
                Type[] kvt = ((ParameterizedType) to).getActualTypeArguments();
                return new AbstractMap.SimpleEntry(
                        context.elementToObject(o.object().get("key"), kvt[0]),
                        context.elementToObject(o.object().get("value"), kvt[0])
                );
            }
            return new AbstractMap.SimpleEntry(
                    context.elementToObject(o.object().get("key"), Object.class),
                    context.elementToObject(o.object().get("value"), Object.class)
            );
        }

    }

    private static class NutsElementFactoryCollection implements NutsElementFactory {

        @Override
        public NutsElement createElement(Object o, Type typeOfSrc, NutsElementFactoryContext context) {
            if (o instanceof List) {
                return new NutsArrayElementFromList((List) o, context);
            }
            return new NutsArrayElementFromCollection((Collection) o, context);
        }

        public Collection fillObject(NutsElement o, Collection coll, Type elemType, Type to, NutsElementFactoryContext context) {
            for (NutsElement nutsElement : o.array().children()) {
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
            if (cls == null) {
                throw new IllegalArgumentException("invalid");
            }
            switch (cls.getName()) {
                case "java.util.Collection":
                case "java.util.List":
                case "java.util.ArrayList": {
                    return fillObject(o, new ArrayList(o.array().size()), elemType, to, context);
                }
            }
            throw new IllegalArgumentException("fix me");
        }

    }

    private static class NutsElementFactoryIterator implements NutsElementFactory<Iterator> {

        @Override
        public NutsElement createElement(Iterator o, Type typeOfSrc, NutsElementFactoryContext context) {
            return new NutsArrayElementFromIterator((Iterator) o, context);
        }

        @Override
        public Iterator createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            Class elemType = Object.class;
            return o.array().children().stream().map(x -> context.elementToObject(x, elemType)).collect(
                    Collectors.toList()
            ).iterator();
        }

    }

    private static class NutsElementFactoryInstant implements NutsElementFactory<Instant> {

        @Override
        public NutsElement createElement(Instant o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.elements().forDate((Instant) o);
        }

        @Override
        public Instant createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            switch (o.type()) {
                case DATE: {
                    return o.primitive().getDate();
                }
                case INTEGER: {
                    return Instant.ofEpochMilli(o.primitive().getInt());
                }
                case LONG: {
                    return Instant.ofEpochMilli(o.primitive().getLong());
                }
                case STRING: {
                    return Instant.parse(o.primitive().getString());
                }
            }
            throw new IllegalArgumentException("unable to parse instant " + o);
        }
    }

    private static class NutsElementFactoryUtilDate implements NutsElementFactory<java.util.Date> {

        @Override
        public NutsElement createElement(java.util.Date o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.elements().forDate(o.toInstant());
        }

        @Override
        public java.util.Date createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            Instant i = (Instant) context.defaultElementToObject(o, Instant.class);
            return new Date(i.toEpochMilli());
        }
    }

    private static class NutsElementFactoryNumber implements NutsElementFactory<Number> {

        @Override
        public NutsElement createElement(Number o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.elements().forNumber((Number) o);
        }

        @Override
        public Number createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            switch (((Class) to).getName()) {
                case "byte":
                case "java.lang.Byte":
                    return o.primitive().getByte();
                case "short":
                case "java.lang.Short":
                    return o.primitive().getShort();
                case "int":
                case "java.lang.Integer":
                    return o.primitive().getInt();
                case "long":
                case "java.lang.Long":
                    return o.primitive().getShort();
                case "float":
                case "java.lang.Float":
                    return o.primitive().getShort();
                case "double":
                case "java.lang.Double":
                    return o.primitive().getShort();
                case "java.lang.BigDecimal":
                    return new BigDecimal(o.primitive().getString());
                case "java.lang.BigInteger":
                    return new BigInteger(o.primitive().getString());
            }
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private static class NutsElementFactoryBoolean implements NutsElementFactory<Boolean> {

        @Override
        public NutsElement createElement(Boolean o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.elements().forBoolean((Boolean) o);
        }

        @Override
        public Boolean createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            switch (((Class) to).getName()) {
                case "boolean":
                case "java.lang.Boolean":
                    return o.primitive().getBoolean();
            }
            throw new UnsupportedOperationException("Not supported.");
        }

    }

    private static class NutsElementFactoryEnum implements NutsElementFactory<Enum> {

        @Override
        public NutsElement createElement(Enum o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.elements().forString(String.valueOf(o));
        }

        @Override
        public Enum createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            switch (o.type()) {
                case BYTE:
                case SHORT:
                case INTEGER:
                case LONG: {
                    NutsPrimitiveElement p = o.primitive();
                    return (Enum) ((Class) to).getEnumConstants()[p.getInt()];
                }
                case STRING: {
                    NutsPrimitiveElement p = o.primitive();
                    return Enum.valueOf(ReflectUtils.getRawClass(to), p.getString());
                }
            }
            throw new IllegalArgumentException("unexpected");
        }
    }

    private static class NutsElementFactoryChar implements NutsElementFactory<Character> {

        @Override
        public NutsElement createElement(Character o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.elements().forString(String.valueOf(o));
        }

        @Override
        public Character createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            final String s = o.primitive().getString();
            return (s==null || s.isEmpty())?
                    (((to instanceof Class) && ((Class)to).isPrimitive()) ? '\0':null)
                    :s.charAt(0)
                    ;
        }
    }

    private static class NutsElementFactoryString implements NutsElementFactory<String> {

        @Override
        public NutsElement createElement(String o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.elements().forString(String.valueOf(o));
        }

        @Override
        public String createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
            return o.primitive().getString();
        }
    }

    private static class NutsElementFactoryNull implements NutsElementFactory<Object> {

        @Override
        public NutsElement createElement(Object o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.elements().forNull();
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

    private static class NutsElementFactoryNutsDefinition implements NutsElementFactory<NutsDefinition> {

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

    private static class NutsElementFactoryNutsClassifierMapping implements NutsElementFactory<NutsClassifierMapping> {

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

    private static class NutsElementFactoryNutsArtifactCall implements NutsElementFactory<NutsArtifactCall> {

        @Override
        public NutsElement createElement(NutsArtifactCall o, Type typeOfSrc, NutsElementFactoryContext context) {
            DefaultNutsArtifactCall dd = (o instanceof DefaultNutsArtifactCall) ? (DefaultNutsArtifactCall) o : new DefaultNutsArtifactCall(o);
            return context.defaultObjectToElement(dd, null);
        }

        @Override
        public NutsArtifactCall createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsObjectElement object = o.object();
            NutsId id = (NutsId) context.elementToObject(object.get("id"), NutsId.class);
            String[] arguments = (String[]) context.elementToObject(object.get("arguments"), String[].class);
            Map<String, String> properties = (Map<String, String>) context.elementToObject(object.get("properties"), ReflectUtils.createParametrizedType(Map.class, String.class, String.class));

            return new DefaultNutsArtifactCall(id, arguments, properties);
        }
    }

    private static class NutsElementFactoryNutsId implements NutsElementFactory<NutsId> {

        @Override
        public NutsElement createElement(NutsId o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.defaultObjectToElement(o.toString(), null);
        }

        @Override
        public NutsId createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            return context.getWorkspace().id().parser().parse(o.primitive().getString());
        }

    }

    private static class NutsElementFactoryNutsVersion implements NutsElementFactory<NutsVersion> {

        @Override
        public NutsElement createElement(NutsVersion o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.defaultObjectToElement(o.toString(), null);
        }

        @Override
        public NutsVersion createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            return context.getWorkspace().version().parser().parse(o.primitive().getString());
        }

    }

    private static class NutsElementFactoryPath implements NutsElementFactory<Path> {

        @Override
        public NutsElement createElement(Path o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.defaultObjectToElement(o.toString(), null);
        }

        @Override
        public Path createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            return Paths.get(o.primitive().getString());
        }
    }

    private static class NutsElementFactoryFile implements NutsElementFactory<File> {

        @Override
        public NutsElement createElement(File o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.defaultObjectToElement(o.toString(), null);
        }

        @Override
        public File createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            return new File(o.primitive().getString());
        }
    }

    private static class NutsElementFactoryNutsDescriptor implements NutsElementFactory<NutsDescriptor> {

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

    private static class NutsElementFactoryNutsSdkLocation implements NutsElementFactory<NutsSdkLocation> {

        @Override
        public NutsElement createElement(NutsSdkLocation o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.defaultObjectToElement(o, null);
        }

        @Override
        public NutsSdkLocation createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsObjectElement obj = o.object();
            NutsId id = (NutsId) context.elementToObject(obj.get("id"), NutsId.class);
            String product = (String) context.elementToObject(obj.get("product"), String.class);
            String name = (String) context.elementToObject(obj.get("name"), String.class);
            String path = (String) context.elementToObject(obj.get("path"), String.class);
            String version = (String) context.elementToObject(obj.get("version"), String.class);
            String packaging = (String) context.elementToObject(obj.get("packaging"), String.class);
            Integer priority = (Integer) context.elementToObject(obj.get("priority"), int.class);
            return new NutsSdkLocation(id, product, name, path, version, packaging, priority == null ? 0 : priority);
        }

    }

    private static class NutsElementFactoryNutsDependency implements NutsElementFactory<NutsDependency> {

        @Override
        public NutsElement createElement(NutsDependency o, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.defaultObjectToElement(context.getWorkspace().dependency().builder().set(o), null
            );
        }

        @Override
        public NutsDependency createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            DefaultNutsDependencyBuilder builder = (DefaultNutsDependencyBuilder) context.defaultElementToObject(o, DefaultNutsDependencyBuilder.class);
            return context.getWorkspace().dependency().builder().set(builder).build();
        }

    }

    private static class NutsElementFactoryNutsIdLocation implements NutsElementFactory<NutsIdLocation> {

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

    private class NutsElemenSerializationAdapterObjReflect implements NutsElementFactory<Object> {

        @Override
        public NutsElement createElement(Object src, Type typeOfSrc, NutsElementFactoryContext context) {
            ReflectType m = typesRepository.getType(typeOfSrc);
            NutsObjectElementBuilder obj = context.elements().forObject();
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
            NutsObjectElement eobj = o.object();
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

    private static class NutsElemenSerializationAdapterArr implements NutsElementFactory<Object> {

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
                        x[i] = e.get(i).primitive().getBoolean();
                    }
                    return x;
                }
                case "byte": {
                    byte[] x = new byte[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).primitive().getByte();
                    }
                    return x;
                }
                case "short": {
                    short[] x = new short[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).primitive().getShort();
                    }
                    return x;
                }
                case "int": {
                    int[] x = new int[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).primitive().getInt();
                    }
                    return x;
                }
                case "long": {
                    long[] x = new long[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).primitive().getLong();
                    }
                    return x;
                }
                case "float": {
                    float[] x = new float[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).primitive().getFloat();
                    }
                    return x;
                }
                case "double": {
                    double[] x = new double[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).primitive().getDouble();
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
            return new NutsArrayElementFromArray(src, context);
        }

    }

    private static class NutsElementFactoryPrimitiveBooleanArray implements NutsElementFactory<boolean[]> {

        public NutsElementFactoryPrimitiveBooleanArray() {
        }

        @Override
        public NutsElement createElement(boolean[] src, Type typeOfSrc, NutsElementFactoryContext context) {
            return new NutsArrayElementFromArray(src, context);
        }

        @Override
        public boolean[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsArrayElement earr = o.array();
            boolean[] arr = new boolean[earr.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (boolean) context.elementToObject(earr.get(i), boolean.class);
            }
            return arr;
        }
    }

    private static class NutsElementFactoryPrimitiveByteArray implements NutsElementFactory<byte[]> {

        public NutsElementFactoryPrimitiveByteArray() {
        }

        @Override
        public NutsElement createElement(byte[] src, Type typeOfSrc, NutsElementFactoryContext context) {
            return new NutsArrayElementFromArray(src, context);
        }

        @Override
        public byte[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsArrayElement earr = o.array();
            byte[] arr = new byte[earr.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (byte) context.elementToObject(earr.get(i), byte.class);
            }
            return arr;
        }
    }

    private static class NutsElementFactoryPrimitiveShortArray implements NutsElementFactory<short[]> {

        public NutsElementFactoryPrimitiveShortArray() {
        }

        @Override
        public NutsElement createElement(short[] src, Type typeOfSrc, NutsElementFactoryContext context) {
            return new NutsArrayElementFromArray(src, context);
        }

        @Override
        public short[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsArrayElement earr = o.array();
            short[] arr = new short[earr.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (short) context.elementToObject(earr.get(i), short.class);
            }
            return arr;
        }
    }

    private static class NutsElementFactoryPrimitiveCharArray implements NutsElementFactory<char[]> {

        public NutsElementFactoryPrimitiveCharArray() {
        }

        @Override
        public NutsElement createElement(char[] src, Type typeOfSrc, NutsElementFactoryContext context) {
            return context.elements().forString(new String(src));
        }

        @Override
        public char[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsArrayElement earr = o.array();
            String s = (String) context.elementToObject(o, String.class);
            return s.toCharArray();
        }
    }

    private static class NutsElementFactoryPrimitiveIntArray implements NutsElementFactory<int[]> {

        public NutsElementFactoryPrimitiveIntArray() {
        }

        @Override
        public NutsElement createElement(int[] src, Type typeOfSrc, NutsElementFactoryContext context) {
            return new NutsArrayElementFromArray(src, context);
        }

        @Override
        public int[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsArrayElement earr = o.array();
            int[] arr = new int[earr.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (int) context.elementToObject(earr.get(i), int.class);
            }
            return arr;
        }
    }

    private static class NutsElementFactoryPrimitiveLongArray implements NutsElementFactory<long[]> {

        public NutsElementFactoryPrimitiveLongArray() {
        }

        @Override
        public NutsElement createElement(long[] src, Type typeOfSrc, NutsElementFactoryContext context) {
            return new NutsArrayElementFromArray(src, context);
        }

        @Override
        public long[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsArrayElement earr = o.array();
            long[] arr = new long[earr.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (long) context.elementToObject(earr.get(i), long.class);
            }
            return arr;
        }
    }

    private static class NutsElementFactoryFloatArray implements NutsElementFactory<float[]> {

        public NutsElementFactoryFloatArray() {
        }

        @Override
        public NutsElement createElement(float[] src, Type typeOfSrc, NutsElementFactoryContext context) {
            return new NutsArrayElementFromArray(src, context);
        }

        @Override
        public float[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsArrayElement earr = o.array();
            float[] arr = new float[earr.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (float) context.elementToObject(earr.get(i), float.class);
            }
            return arr;
        }
    }

    private static class NutsElementFactoryPrimitiveDoubleArray implements NutsElementFactory<double[]> {

        public NutsElementFactoryPrimitiveDoubleArray() {
        }

        @Override
        public NutsElement createElement(double[] src, Type typeOfSrc, NutsElementFactoryContext context) {
            return new NutsArrayElementFromArray(src, context);
        }

        @Override
        public double[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsArrayElement earr = o.array();
            double[] arr = new double[earr.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (double) context.elementToObject(earr.get(i), double.class);
            }
            return arr;
        }
    }

    private static class NutsElementFactoryObjectArray implements NutsElementFactory<Object[]> {

        public NutsElementFactoryObjectArray() {
        }

        @Override
        public NutsElement createElement(Object[] src, Type typeOfSrc, NutsElementFactoryContext context) {
            return new NutsArrayElementFromArray(src, context);
        }

        @Override
        public Object[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            NutsArrayElement earr = o.array();
            Object[] arr = new Object[earr.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (Object) context.elementToObject(earr.get(i), Object.class);
            }
            return arr;
        }
    }

    private static class NutsElementFactoryNutsPrimitiveElement implements NutsElementFactory<NutsPrimitiveElement> {

        public NutsElementFactoryNutsPrimitiveElement() {
        }

        @Override
        public NutsElement createElement(NutsPrimitiveElement src, Type typeOfSrc, NutsElementFactoryContext context) {
            return src;
        }

        @Override
        public NutsPrimitiveElement createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            if (o.type().isPrimitive()) {
                return o.primitive();
            }
            return context.elements().forString(o.toString());
        }
    }

    private static class NutsElementFactoryNutsArrayElement implements NutsElementFactory<NutsArrayElement> {

        public NutsElementFactoryNutsArrayElement() {
        }

        @Override
        public NutsElement createElement(NutsArrayElement src, Type typeOfSrc, NutsElementFactoryContext context) {
            return src;
        }

        @Override
        public NutsArrayElement createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            if (o.type() == NutsElementType.ARRAY) {
                return o.array();
            }
            return context.elements().forArray().add(o).build();
        }
    }

    private static class NutsElementFactoryNutsObjectElement implements NutsElementFactory<NutsObjectElement> {

        public NutsElementFactoryNutsObjectElement() {
        }

        @Override
        public NutsElement createElement(NutsObjectElement src, Type typeOfSrc, NutsElementFactoryContext context) {
            return src;
        }

        @Override
        public NutsObjectElement createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
            if (o.type() == NutsElementType.OBJECT) {
                return o.object();
            }
            return context.elements().forObject().set("value", o).build();
        }
    }

    private static class NutsElementFactoryNutsElement implements NutsElementFactory<NutsElement> {

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

}
