package net.thevpc.nuts.runtime.standalone.tson.impl.marshall;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.marshall.reflect.JavaWord;
import net.thevpc.nuts.runtime.standalone.tson.impl.util.TsonUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Pattern;

public class TsonSerializerConfig {

    private final ClassMap<TsonObjectToElement> objToElem = new ClassMap<TsonObjectToElement>(Object.class, TsonObjectToElement.class);
    private final Map<TypeElementSignature, TsonElementToObject> elemToObj = new HashMap<>();
    private JavaWord javaWord = new JavaWord();

    private static TypeElementSignature[] CACHED = new TypeElementSignature[TsonElementType.values().length];
    private final TsonObjectToElement objToElem_arr = new TsonObjectToElement() {
        @Override
        public TsonElement toElement(Object object, TsonObjectContext context) {
            TsonArrayBuilder a = Tson.ofArrayBuilder();
            int len = Array.getLength(object);
            for (int i = 0; i < len; i++) {
                a.add(context.elem(Array.get(object, i)));
            }
            return a.build();
        }
    };

    private final TsonElementToObject elemToObj_arr = new TsonElementToObject() {
        @Override
        public Object toObject(TsonElement element, Class to, TsonObjectContext context) {
            TsonArray arr = (TsonArray) element;
            Object oarr = Array.newInstance(to.getComponentType(), arr.size());
            int size = 0;
            for (TsonElement tsonElement : arr) {
                Array.set(oarr, size, context.obj(tsonElement, null));
                size++;
            }
            return null;
        }
    };


    private ClassPropertiesRegistry classPropertiesRegistry;

    static {
        for (TsonElementType value : TsonElementType.values()) {
            CACHED[value.ordinal()] = new TypeElementSignature(value, null, null);
        }
    }

    public TsonSerializerConfig(TsonSerializerConfig other) {
        if (other != null) {
            elemToObj.putAll(other.elemToObj);
            objToElem.putAll(other.objToElem);
            classPropertiesRegistry = other.classPropertiesRegistry;
        } else {
            classPropertiesRegistry = ClassPropertiesRegistry.DEFAULT;
        }
    }

    public TsonSerializerConfig() {
        classPropertiesRegistry = ClassPropertiesRegistry.DEFAULT;
    }

    public void registerDefaults() {
        registerObjToElemConverter(TsonSerializable.class, TsonSerializable::toTsonElement);
        registerObjToElemConverter(TsonElementBase.class, (object, context) -> object.build());
        registerObjToElemConverter(Enum.class, (object, context) -> Tson.ofName(object.name()));
        registerObjToElemConverter(String.class, (object, context) -> {
            if (context.isPreferName()) {
                String s = (String) object;
                if (s != null && s.matches("^[a-zA-Z_]+[a-zA-Z_0-9_]+$")) {
                    return Tson.ofName(s);
                }
            }
            return Tson.of((String) object);
        });
        registerObjToElemConverter(Double.class, (object, context) -> Tson.of((Double) object));
        registerObjToElemConverter(Double.TYPE, (object, context) -> Tson.of((Double) object));
        registerObjToElemConverter(Float.class, (object, context) -> Tson.of((Float) object));
        registerObjToElemConverter(Float.TYPE, (object, context) -> Tson.of((Float) object));
        registerObjToElemConverter(Long.class, (object, context) -> Tson.of((Long) object));
        registerObjToElemConverter(Long.TYPE, (object, context) -> Tson.of((Long) object));
        registerObjToElemConverter(Integer.class, (object, context) -> Tson.of((Integer) object));
        registerObjToElemConverter(Integer.TYPE, (object, context) -> Tson.of((Integer) object));
        registerObjToElemConverter(Short.class, (object, context) -> Tson.of((Short) object));
        registerObjToElemConverter(Short.TYPE, (object, context) -> Tson.of((Short) object));
        registerObjToElemConverter(Byte.class, (object, context) -> Tson.of((Byte) object));
        registerObjToElemConverter(Byte.TYPE, (object, context) -> Tson.of((Byte) object));
        registerObjToElemConverter(Character.class, (object, context) -> Tson.of((Character) object));
        registerObjToElemConverter(Character.TYPE, (object, context) -> Tson.of((Character) object));
        registerObjToElemConverter(Date.class, (object, context) -> Tson.of((Date) object));
        registerObjToElemConverter(Instant.class, (object, context) -> Tson.of((Instant) object));
        registerObjToElemConverter(Time.class, (object, context) -> Tson.of((Time) object));
        registerObjToElemConverter(LocalTime.class, (object, context) -> Tson.of((LocalTime) object));
        registerObjToElemConverter(LocalDate.class, (object, context) -> Tson.of((LocalDate) object));
        registerObjToElemConverter(Pattern.class, (object, context) -> Tson.of((Pattern) object));
        registerObjToElemConverter(Boolean.class, (object, context) -> Tson.of((Boolean) object));
        registerObjToElemConverter(Boolean.TYPE, (object, context) -> Tson.of((Boolean) object));
        registerObjToElemConverter(Collection.class, (object, context) -> {
            TsonArrayBuilder a = Tson.ofArrayBuilder();
            for (Object o : object) {
                a.add(context.elem(o));
            }
            return a.build();
        });
        registerObjToElemConverter(Map.class, (object, context) -> {
            TsonObjectBuilder a = Tson.ofObjectBuilder();
            for (Map.Entry<?, ?> o : ((Map<?, ?>) object).entrySet()) {
                a.add(
                        context.elem(o.getKey()),
                        context.elem(o.getValue())
                );
            }
            return a.build();
        });

        registerObjToElemConverter(Map.Entry.class, (object, context) -> {
            Map.Entry me = (Map.Entry) object;
            return Tson.ofPair(
                    context.elem(me.getKey()),
                    context.elem(me.getValue())
            );
        });

        registerObjToElemConverter(Object.class, (object, context) -> {
            TsonObjectBuilder a = Tson.ofObjectBuilder();
            ClassPropertiesRegistry.ClassInfo ci = classPropertiesRegistry.getClassInfo(object.getClass());
            for (ClassPropertiesRegistry.TypeProperty o : ci.getProperties(true)) {
                a.add(
                        context.copy().setPreferName(true).elem(o.name()),
                        context.elem(o.get(object))
                );
            }
            return a.build();
        });

        registerElemToObjConverter(TsonElementType.NULL, null, null, (element, to, context) -> null);

        registerElemToObjConverter(TsonElementType.DOUBLE_QUOTED_STRING, null, null, (element, to, context) -> element.stringValue());
        registerElemToObjConverter(TsonElementType.SINGLE_QUOTED_STRING, null, null, (element, to, context) -> element.stringValue());
        registerElemToObjConverter(TsonElementType.ANTI_QUOTED_STRING, null, null, (element, to, context) -> element.stringValue());
        registerElemToObjConverter(TsonElementType.TRIPLE_DOUBLE_QUOTED_STRING, null, null, (element, to, context) -> element.stringValue());
        registerElemToObjConverter(TsonElementType.TRIPLE_SINGLE_QUOTED_STRING, null, null, (element, to, context) -> element.stringValue());
        registerElemToObjConverter(TsonElementType.TRIPLE_ANTI_QUOTED_STRING, null, null, (element, to, context) -> element.stringValue());
        registerElemToObjConverter(TsonElementType.LINE_STRING, null, null, (element, to, context) -> element.stringValue());

        registerElemToObjConverter(TsonElementType.NAME, null, null, (element, to, context) -> element.stringValue());
        registerElemToObjConverter(TsonElementType.INTEGER, null, null, (element, to, context) -> element.intValue());
        registerElemToObjConverter(TsonElementType.LONG, null, null, (element, to, context) -> element.longValue());
        registerElemToObjConverter(TsonElementType.SHORT, null, null, (element, to, context) -> element.shortValue());
        registerElemToObjConverter(TsonElementType.BYTE, null, null, (element, to, context) -> element.byteValue());
        registerElemToObjConverter(TsonElementType.BOOLEAN, null, null, new BooleanTsonElementToObject());
        registerElemToObjConverter(TsonElementType.CHAR, null, null, (element, to, context) -> element.charValue());
        registerElemToObjConverter(TsonElementType.FLOAT, null, null, (element, to, context) -> element.floatValue());
        registerElemToObjConverter(TsonElementType.DOUBLE, null, null, (element, to, context) -> element.doubleValue());

        registerElemToObjConverter(TsonElementType.LOCAL_DATETIME, null, null, (element, to, context) -> element.localDateTimeValue());
        registerElemToObjConverter(TsonElementType.LOCAL_DATETIME, null, Instant.class, (element, to, context) -> element.localDateTimeValue());
        registerElemToObjConverter(TsonElementType.LOCAL_DATETIME, null, Date.class, (element, to, context) -> TsonUtils.convertToDate(element.localDateTimeValue()));

        registerElemToObjConverter(TsonElementType.LOCAL_DATE, null, null, (element, to, context) -> element.localDateValue());
        registerElemToObjConverter(TsonElementType.LOCAL_DATE, null, LocalDate.class, (element, to, context) -> element.localDateValue());
        registerElemToObjConverter(TsonElementType.LOCAL_DATE, null, Date.class, (element, to, context) -> TsonUtils.convertToDate(element.localDateValue()));
        registerElemToObjConverter(TsonElementType.LOCAL_DATE, null, java.sql.Date.class, (element, to, context) -> TsonUtils.convertToSqlDate(element.localDateValue()));

        registerElemToObjConverter(TsonElementType.LOCAL_TIME, null, null, (element, to, context) -> element.localTimeValue());
        registerElemToObjConverter(TsonElementType.LOCAL_TIME, null, LocalTime.class, (element, to, context) -> element.localTimeValue());
        registerElemToObjConverter(TsonElementType.LOCAL_TIME, null, Time.class, (element, to, context) -> TsonUtils.convertToSqlTime(element.localTimeValue()));

        registerElemToObjConverter(TsonElementType.REGEX, null, null, (element, to, context) -> element.regexValue());

        registerElemToObjConverter(TsonElementType.PAIR, null, null, (element1, to, context1) -> keyValueToMapEntry(element1, to, context1));
        registerElemToObjConverter(TsonElementType.ARRAY, null, null, (element, to, context) -> {
            return convertDefaultArray(element, to, context);
        });
        registerElemToObjConverter(TsonElementType.NAMED_ARRAY, null, null, (element, to, context) -> {
            return convertDefaultArray(element, to, context);
        });
        registerElemToObjConverter(TsonElementType.NAMED_PARAMETRIZED_ARRAY, null, null, (element, to, context) -> {
            return convertDefaultArray(element, to, context);
        });
        registerElemToObjConverter(TsonElementType.PARAMETRIZED_ARRAY, null, null, (element, to, context) -> {
            return convertDefaultArray(element, to, context);
        });
        registerElemToObjConverter(TsonElementType.OBJECT, null, null, (element, to, context) -> {
            return convertDefaultObject(element, to, context);
        });
        registerElemToObjConverter(TsonElementType.NAMED_OBJECT, null, null, (element, to, context) -> {
            return convertDefaultObject(element, to, context);
        });
        registerElemToObjConverter(TsonElementType.PARAMETRIZED_OBJECT, null, null, (element, to, context) -> {
            return convertDefaultObject(element, to, context);
        });
        registerElemToObjConverter(TsonElementType.NAMED_PARAMETRIZED_OBJECT, null, null, (element, to, context) -> {
            return convertDefaultObject(element, to, context);
        });

        registerElemToObjConverter(TsonElementType.UPLET, null, null, (element, to, context) -> {
            if (to == null || to.equals(Map.class)) {
                TsonUplet ee = element.toUplet();
                return arrayElementToObject(ee.params().toList(), null, context);
            }
            return customDeserializer(to)
                    .configureLenient()
                    .setInstanceFactory(context12 -> classPropertiesRegistry.getClassInfo(to).newInstance())
                    .toObject(element, to, context);
        });
        registerElemToObjConverter(TsonElementType.NAMED_UPLET, null, null, (element, to, context) -> {
            if (to == null || to.equals(Map.class)) {
                TsonUplet ee = element.toUplet();
                Map<Object, Object> mmap = new LinkedHashMap<>();
                Class keyType = null;
                Class valType = null;
                for (TsonElement entry : ee.body()) {
                    if (entry.isPair()) {
                        TsonPair pair = entry.toPair();
                        mmap.put(
                                context.obj(pair.key(), keyType),
                                context.obj(pair.value(), valType)
                        );
                    } else {
                        mmap.put(
                                context.obj(entry, keyType),
                                null
                        );
                    }
                }
                return mmap;
            }
            return customDeserializer(to)
                    .configureLenient()
                    .setInstanceFactory(context12 -> classPropertiesRegistry.getClassInfo(to).newInstance())
                    .toObject(element, to, context);
        });

    }

    private Object convertDefaultObject(TsonElement element, Class<Object> to, TsonObjectContext context) {
        if (to == null || to.equals(Map.class)) {
            TsonObject ee = element.toObject();
            Map<Object, Object> mmap = new LinkedHashMap<>();
            Class keyType = null;
            Class valType = null;
            for (TsonElement entry : ee.body()) {
                if (entry.isPair()) {
                    TsonPair pair = entry.toPair();
                    mmap.put(
                            context.obj(pair.key(), keyType),
                            context.obj(pair.value(), valType)
                    );
                } else {
                    mmap.put(
                            context.obj(entry, keyType),
                            null
                    );
                }
            }
            return mmap;
        }
        return customDeserializer(to)
                .configureLenient()
                .setInstanceFactory(context12 -> classPropertiesRegistry.getClassInfo(to).newInstance())
                .toObject(element, to, context);
    }

    private Object convertDefaultArray(TsonElement element, Class<Object> to, TsonObjectContext context) {
        if (to == null || to.equals(Map.class)) {
            TsonArray ee = element.toArray();
            Map<String, Object> namedArray = new LinkedHashMap<>();
            if(ee.isNamed()) {
                namedArray.put("name", ee.name());
            }
            if(ee.isParametrized()) {
                namedArray.put("args", arrayElementToObject(ee.params(), to, context));
            }
            namedArray.put("values", arrayElementToObject(ee.body(), to, context));
            return namedArray;
        }
        TsonArray ee = element.toArray();
        return arrayElementToObject(ee.body(), to, context);
    }

    private Object arrayElementToObject(TsonElement element, Class<?> to, TsonObjectContext context) {
        return arrayElementToObject(element.toArray().body(), to, context);
    }

    private Object objectElementToObject(TsonElement element, Class<?> to, TsonObjectContext context) {
        return objectElementToObject(element.toObject().body(), to, context);
    }

    private Object arrayElementToObject(List<TsonElement> elements, Class<?> to, TsonObjectContext context) {
        return arrayElementToObject(elements == null ? null : elements.toArray(new TsonElement[0]), to, context);
    }

    private Object arrayElementToObject(TsonElementList elements, Class<?> to, TsonObjectContext context) {
        return arrayElementToObject(elements == null ? null : elements.toArray(), to, context);
    }

    private Object arrayElementToObject(TsonElement[] elements, Class<?> to, TsonObjectContext context) {
        Collection coll = null;
        if (to == null) {
            coll = new ArrayList();
        } else {
            if (to.isArray()) {
                coll = new ArrayList();
                for (TsonElement a : elements) {
                    coll.add(context.obj(a, to.getComponentType()));
                }
                try {
                    return coll.toArray((Object[]) Array.newInstance(to.getComponentType(), coll.size()));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unable to instantiate array", e);
                }
            } else {
                switch (to.getName()) {
                    case "java.util.Collection":
                    case "java.util.List": {
                        coll = new ArrayList();
                        break;
                    }
                    case "java.util.Set": {
                        coll = new HashSet();
                        break;
                    }
                    case "java.util.Queue": {
                        coll = new PriorityQueue();
                        break;
                    }
                    case "java.util.Deque": {
                        coll = new LinkedList();
                        break;
                    }
                    default: {
                        if (Collection.class.isAssignableFrom(to)) {
                            try {
                                coll = (Collection) to.newInstance();
                            } catch (Exception e) {
                                throw new IllegalArgumentException("Unable to create collection");
                            }
                        }
                        break;
                    }
                }
            }
        }
        if (coll == null) {
            throw new IllegalArgumentException("Unable to convert to " + to);
        }
        for (TsonElement a : elements) {
            coll.add(context.obj(a, null));
        }
        return coll;
    }

    private Object objectElementToObject(List<TsonElement> elements, Class to, TsonObjectContext context) {
        return objectElementToObject(elements == null ? null : elements.toArray(new TsonElement[0]), to, context);
    }

    private Object objectElementToObject(TsonElementList elements, Class to, TsonObjectContext context) {
        return objectElementToObject(elements == null ? null : elements.toArray(), to, context);
    }

    private Object objectElementToObject(TsonElement[] elements, Class to, TsonObjectContext context) {
        Map coll = null;
        if (to == null) {
            coll = new HashMap();
        } else {
            if (to.isArray()) {
                return arrayElementToObject(elements, to, context);
            } else {
                switch (to.getName()) {
                    case "java.util.Collection":
                    case "java.util.List":
                    case "java.util.Set":
                    case "java.util.Queue":
                    case "java.util.Deque": {
                        return arrayElementToObject(elements, to, context);
                    }
                    case "java.util.Map": {
                        return arrayElementToObject(elements, to, context);
                    }
                    default: {
                        if (Collection.class.isAssignableFrom(to)) {
                            return arrayElementToObject(elements, to, context);
                        }
                        if (Map.class.isAssignableFrom(to)) {
                            try {
                                coll = (Map) to.newInstance();
                            } catch (Exception e) {
                                throw new IllegalArgumentException("Unable to create map");
                            }
                        }
                        break;
                    }
                }
            }
        }
        if (coll == null) {
            ClassPropertiesRegistry.ClassInfo ci = classPropertiesRegistry.getClassInfo(to);
            Object o = ci.newInstance();
            for (TsonElement a : elements) {
                if (a.type() == TsonElementType.PAIR) {
                    TsonPair kv = a.toPair();
                    Object u = context.obj(kv.key(), null);
                    if (u instanceof String) {
                        ClassPropertiesRegistry.TypeProperty property = ci.getProperty((String) u, true);
                        if (property == null) {
                            throw new IllegalArgumentException("Property not found " + u + " in " + ci.name());
                        }
                        property.set(o, context.obj(kv.value(), property.type()));
                    } else {
                        throw new IllegalArgumentException("Unable to initialize object with non name property");
                    }
                } else {
                    throw new IllegalArgumentException("Unable to initialize object with non name property");
                }
            }
            return o;
        }
        for (TsonElement a : elements) {
            if (a.type() == TsonElementType.PAIR) {
                TsonPair kv = a.toPair();
                coll.put(
                        context.obj(kv.key(), null),
                        context.obj(kv.value(), null)
                );
            } else {
                coll.put(
                        context.obj(a, null),
                        context.obj(Tson.ofNull(), null)
                );
            }
        }
        return coll;
    }

    private static Object keyValueToMapEntry(TsonElement element, Class to, TsonObjectContext context) {
        return new Map.Entry() {
            @Override
            public Object getKey() {
                return context.obj(element.toPair().key(), null);
            }

            @Override
            public Object getValue() {
                return context.obj(element.toPair().value(), null);
            }

            @Override
            public Object setValue(Object value) {
                throw new UnsupportedOperationException();
            }
        };
    }

    public <T> TsonCustomDeserializer<T> customDeserializer(Class<T> to) {
        return new CustomTsonObjectDeserializerImpl<>(javaWord, to);
    }

    public final <T> void registerElemToObjConverter(TsonElementType type, String name, Class to, TsonElementToObject<T> converter) {
        switch (type) {
            case ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case PARAMETRIZED_ARRAY:
            case NAMED_ARRAY:

            case OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            case NAMED_OBJECT:
            case PARAMETRIZED_OBJECT:
            case UPLET:
            case NAMED_UPLET:
            {
                break;
            }
            default: {
                name = null;
            }
        }
        TypeElementSignature sig = new TypeElementSignature(type, name, to);
        if (converter == null) {
            elemToObj.remove(sig);
        } else {
            elemToObj.put(sig, converter);
        }
    }

    public final <T> void registerObjToElemConverter(Class<T> type, TsonObjectToElement<T> writer) {
        if (writer == null) {
            objToElem.remove(type);
        } else {
            objToElem.put(type, writer);
        }
    }

    public <T> TsonObjectToElement<T> getObjToElemConverter(Class<T> c) {
        if (c.isArray()) {
            return objToElem_arr;
        }
        TsonObjectToElement<T> w = objToElem.get(c);
        if (w == null) {
            throw new NoSuchElementException("missing object to element converter for " + c);
        }
        return w;
    }

    public void writeDefaultElement(Tson element, Writer writer) {
        try {
            writer.write(element.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

//    protected TsonElement objectToElement(Object any) {
//        return defaultObjectToElement(any, new DefaultTsonObjectContext(this));
//    }


    private TypeElementSignature[] sig(TsonElement e, Class to) {
        final TsonElementType etype = e.type();
        switch (etype) {
            case ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case PARAMETRIZED_ARRAY:
            case NAMED_ARRAY:
            {
                TsonArray h = e.toArray();
                String name = h == null ? null : h.name();
                LinkedHashSet<TypeElementSignature> all = new LinkedHashSet<>();
                all.add(new TypeElementSignature(etype, name, to));
                all.add(new TypeElementSignature(etype, null, to));
                all.add(new TypeElementSignature(etype, name, null));
                all.add(new TypeElementSignature(etype, null, null));
                return all.toArray(new TypeElementSignature[0]);
            }
            case OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            case NAMED_OBJECT:
            case PARAMETRIZED_OBJECT:
            {
                TsonObject h = e.toObject();
                String name = h == null ? null : h.name();
                LinkedHashSet<TypeElementSignature> all = new LinkedHashSet<>();
                all.add(new TypeElementSignature(etype, name, to));
                all.add(new TypeElementSignature(etype, null, to));
                all.add(new TypeElementSignature(etype, name, null));
                all.add(new TypeElementSignature(etype, null, null));
                return all.toArray(new TypeElementSignature[0]);
            }
            case UPLET:
            case NAMED_UPLET:
            {
                TsonUplet h = e.toUplet();
                if (h.isNamed()) {
                    String name = h == null ? null : h.name();
                    LinkedHashSet<TypeElementSignature> all = new LinkedHashSet<>();
                    all.add(new TypeElementSignature(etype, name, to));
                    all.add(new TypeElementSignature(etype, null, to));
                    all.add(new TypeElementSignature(etype, name, null));
                    all.add(new TypeElementSignature(etype, null, null));
                    return all.toArray(new TypeElementSignature[0]);
                }
                break;
            }
        }
        if (to == null) {
            return new TypeElementSignature[]{CACHED[etype.ordinal()]};
        }
        LinkedHashSet<TypeElementSignature> all = new LinkedHashSet<>();
        all.add(new TypeElementSignature(etype, null, to));
        all.add(new TypeElementSignature(etype, null, null));
        return all.toArray(new TypeElementSignature[0]);
    }


    public <T> TsonElementToObject<T> getElemToObj(TsonElement e, Class<T> to) {
        for (TypeElementSignature ss : sig(e, to)) {
            TsonElementToObject<T> v = elemToObj.get(ss);
            if (v != null) {
                return v;
            }
        }
        return null;
    }


    public final static class TypeElementSignature {

        private final TsonElementType type;
        private final String name;
        private final Class to;
        private final int hash;

        public TypeElementSignature(TsonElementType type, String name, Class to) {
            this.type = type;
            this.name = name;
            this.to = to;
            this.hash = Objects.hash(type, name, to);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TypeElementSignature that = (TypeElementSignature) o;
            return type == that.type
                    && Objects.equals(name, that.name)
                    && Objects.equals(to, that.to);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    public TsonSerializerConfig copy() {
        return new TsonSerializerConfig(this);
    }

    private static class BooleanTsonElementToObject implements TsonElementToObject<Object> {
        @Override
        public Object toObject(TsonElement element, Class<Object> to, TsonObjectContext context) {
            if (to == null) {
                return element.booleanValue();
            }
            switch (to.getName()) {
                case "boolean":
                case "java.lang.Boolean":
                    return element.booleanValue();
                case "byte":
                case "java.lang.Byte":
                    return element.booleanValue() ? (byte) 1 : (byte) 0;
                case "short":
                case "java.lang.Short":
                    return element.booleanValue() ? (short) 1 : (short) 0;
                case "int":
                case "java.lang.Integer":
                    return element.booleanValue() ? 1 : 0;
                case "long":
                case "java.lang.Long":
                    return element.booleanValue() ? (long) 1 : (long) 0;
                case "float":
                case "java.lang.Float":
                    return element.booleanValue() ? (float) 1 : (float) 0;
                case "double":
                case "java.lang.Double":
                    return element.booleanValue() ? (double) 1 : (double) 0;
                case "java.lang.String":
                    return String.valueOf(element.booleanValue());
            }
            throw new IllegalArgumentException("Unable to convert boolean to " + to);
        }
    }

    public JavaWord getJavaWord() {
        return javaWord;
    }
}
