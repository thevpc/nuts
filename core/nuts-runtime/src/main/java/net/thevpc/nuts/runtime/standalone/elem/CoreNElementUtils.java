package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NFragmentElement;
import net.thevpc.nuts.elem.NPairElement;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNPairElement;
import net.thevpc.nuts.text.NFormatted;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NMsg;

import java.lang.reflect.*;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.function.Predicate;

public class CoreNElementUtils {

    //    public static final NutsPrimitiveElement NULL = new DefaultNPrimitiveElement(NutsElementType.NULL, null);
//    public static final NutsPrimitiveElement TRUE = new DefaultNPrimitiveElement(NutsElementType.BOOLEAN, true);
//    public static final NutsPrimitiveElement FALSE = new DefaultNPrimitiveElement(NutsElementType.BOOLEAN, false);
    public static Predicate<Type> DEFAULT_SIMPLE_TYPE = new Predicate<Type>() {
        @Override
        public boolean test(Type type) {
            if (type == null) {
                return true;
            }
            if (type instanceof Class<?>) {
                Class cls = (Class) type;
                switch (cls.getName()) {
                    case "boolean":
                    case "byte":
                    case "char":
                    case "short":
                    case "int":
                    case "long":
                    case "float":
                    case "double":
                    case "java.lang.Character":
                    case "java.lang.String":
                    case "java.lang.StringBuilder":
                    case "java.lang.Boolean":
                    case "java.lang.Byte":
                    case "java.lang.Short":
                    case "java.lang.Integer":
                    case "java.lang.Long":
                    case "java.lang.Float":
                    case "java.lang.Double":
                    case "java.math.BigDecimal":
                    case "java.math.BigInteger":
                    case "java.util.Date":
                    case "java.sql.Time":
                    case "net.thevpc.nuts.math.NDoubleComplex":
                    case "net.thevpc.nuts.math.NFloatComplex":
                    case "net.thevpc.nuts.math.NBigComplex":
                        return true;
                }
                if (Temporal.class.isAssignableFrom(cls)) {
                    return true;
                }
                if (java.util.Date.class.isAssignableFrom(cls)) {
                    return true;
                }
                if (NElement.class.isAssignableFrom(cls)) {
                    return true;
                }
                return (
                        NText.class.isAssignableFrom(cls)
                                || NElement.class.isAssignableFrom(cls)
                                || NFormatted.class.isAssignableFrom(cls)
                                || NMsg.class.isAssignableFrom(cls)
                );
            } else if (type instanceof ParameterizedType) {
                // e.g. List<String> -> get raw type List
                Type rawType = ((ParameterizedType) type).getRawType();
                if (rawType instanceof Class<?>) {
                    return test((Class<?>) rawType);
                } else {
                    throw new IllegalArgumentException("Unexpected raw type: " + rawType);
                }
            } else if (type instanceof GenericArrayType) {
                return false;
            } else if (type instanceof TypeVariable) {
                return false;
            } else if (type instanceof WildcardType) {
                // e.g. ? extends Number or ? super Integer
                WildcardType wildcardType = (WildcardType) type;
                Type[] upperBounds = wildcardType.getUpperBounds();
                // Usually use upper bounds, fallback to Object
                if (upperBounds.length == 0) {
                    return test(Object.class);
                }
                return test(upperBounds[0]);
            } else {
                throw new IllegalArgumentException("Unknown Type: " + type);
            }
        }
    };
    //    private List<NTruthPredicate<Type>> undestructubles=new ArrayList<>(
//            Arrays.asList(
//                    new NTruthPredicate[]{
//                            new NTruthPredicate<Type>() {
//                                @Override
//                                public NTruth test(Type type) {
//                                    if (type == null) {
//                                        return NTruth.TRUE;
//                                    }
//                                    if (type instanceof Class<?>) {
//                                        Class cls = (Class) type;
//                                        switch (cls.getName()) {
//                                            case "boolean":
//                                            case "byte":
//                                            case "char":
//                                            case "short":
//                                            case "int":
//                                            case "long":
//                                            case "float":
//                                            case "double":
//                                            case "java.lang.Character":
//                                            case "java.lang.String":
//                                            case "java.lang.StringBuilder":
//                                            case "java.lang.Boolean":
//                                            case "java.lang.Byte":
//                                            case "java.lang.Short":
//                                            case "java.lang.Integer":
//                                            case "java.lang.Long":
//                                            case "java.lang.Float":
//                                            case "java.lang.Double":
//                                            case "java.math.BigDecimal":
//                                            case "java.math.BigInteger":
//                                            case "java.util.Date":
//                                            case "java.sql.Time":
//                                            case "net.thevpc.nuts.math.NDoubleComplex":
//                                            case "net.thevpc.nuts.math.NFloatComplex":
//                                            case "net.thevpc.nuts.math.NBigComplex":
//                                                return NTruth.TRUE;
//                                        }
//                                        if (Temporal.class.isAssignableFrom(cls)) {
//                                            return true;
//                                        }
//                                        if (java.util.Date.class.isAssignableFrom(cls)) {
//                                            return true;
//                                        }
//                                        if (NElement.class.isAssignableFrom(cls)) {
//                                            return true;
//                                        }
//                                        return (
//                                                NText.class.isAssignableFrom(cls)
//                                                        || NElement.class.isAssignableFrom(cls)
//                                                        || NFormattable.class.isAssignableFrom(cls)
//                                                        || NMsg.class.isAssignableFrom(cls)
//                                        );
//                                    } else if (type instanceof ParameterizedType) {
//                                        // e.g. List<String> -> get raw type List
//                                        Type rawType = ((ParameterizedType) type).getRawType();
//                                        if (rawType instanceof Class<?>) {
//                                            return test((Class<?>) rawType);
//                                        } else {
//                                            throw new IllegalArgumentException("Unexpected raw type: " + rawType);
//                                        }
//                                    } else if (type instanceof GenericArrayType) {
//                                        return false;
//                                    } else if (type instanceof TypeVariable) {
//                                        return false;
//                                    } else if (type instanceof WildcardType) {
//                                        // e.g. ? extends Number or ? super Integer
//                                        WildcardType wildcardType = (WildcardType) type;
//                                        Type[] upperBounds = wildcardType.getUpperBounds();
//                                        // Usually use upper bounds, fallback to Object
//                                        if (upperBounds.length == 0) {
//                                            return test(Object.class);
//                                        }
//                                        return test(upperBounds[0]);
//                                    } else {
//                                        throw new IllegalArgumentException("Unknown Type: " + type);
//                                    }
//                                }
//                            }
//                    }
//            )
//    );
    public static Predicate<Type> DEFAULT_INDESTRUCTIBLE = new Predicate<Type>() {
        @Override
        public boolean test(Type type) {
            if (type == null) {
                return true;
            }
            if (type instanceof Class<?>) {
                Class cls = (Class) type;
                switch (cls.getName()) {
                    case "boolean":
                    case "byte":
                    case "char":
                    case "short":
                    case "int":
                    case "long":
                    case "float":
                    case "double":
                    case "java.lang.Character":
                    case "java.lang.String":
                    case "java.lang.StringBuilder":
                    case "java.lang.Boolean":
                    case "java.lang.Byte":
                    case "java.lang.Short":
                    case "java.lang.Integer":
                    case "java.lang.Long":
                    case "java.lang.Float":
                    case "java.lang.Double":
                    case "java.math.BigDecimal":
                    case "java.math.BigInteger":
                    case "java.util.Date":
                    case "java.sql.Time":
                    case "net.thevpc.nuts.math.NDoubleComplex":
                    case "net.thevpc.nuts.math.NFloatComplex":
                    case "net.thevpc.nuts.math.NBigComplex":
                        return true;
                }
                if (Temporal.class.isAssignableFrom(cls)) {
                    return true;
                }
                if (java.util.Date.class.isAssignableFrom(cls)) {
                    return true;
                }
                return (
                        NText.class.isAssignableFrom(cls)
                                || NElement.class.isAssignableFrom(cls)
                                || NFormatted.class.isAssignableFrom(cls)
                                || NMsg.class.isAssignableFrom(cls)
                );
            } else if (type instanceof ParameterizedType) {
                // e.g. List<String> -> get raw type List
                Type rawType = ((ParameterizedType) type).getRawType();
                if (rawType instanceof Class<?>) {
                    return test((Class<?>) rawType);
                } else {
                    throw new IllegalArgumentException("Unexpected raw type: " + rawType);
                }
            } else if (type instanceof GenericArrayType) {
                return false;
            } else if (type instanceof TypeVariable) {
                return false;
            } else if (type instanceof WildcardType) {
                // e.g. ? extends Number or ? super Integer
                WildcardType wildcardType = (WildcardType) type;
                Type[] upperBounds = wildcardType.getUpperBounds();
                // Usually use upper bounds, fallback to Object
                if (upperBounds.length == 0) {
                    return test(Object.class);
                }
                return test(upperBounds[0]);
            } else {
                throw new IllegalArgumentException("Unknown Type: " + type);
            }
        }
    };

    public static List<NElement> denullList(NElement e) {
        if (e == null) {
            return Collections.singletonList(NElement.ofNull());
        }
        if (e.isFragment()) {
            return e.asFragment().get().children();
        }
        return Collections.singletonList(e);
    }

    public static List<NElement> setAt(int index, NElement e, List<NElement> values) {
        if (values == null) {
            values = new ArrayList<>();
        }
        while (values.size() < index) {
            values.add(NElement.ofNull());
        }

        if (index < values.size()) {
            values.remove(index);
        }
        values.addAll(index, CoreNElementUtils.denullList(e));
        return values;
    }

    public static List<NElement> setAll(Collection<NElement> some, List<NElement> values) {
        if (some == null) {
            return null;
        }
        values.clear();
        return addAll(some, values);
    }

    public static List<NElement> addAll(Collection<NElement> some, List<NElement> values) {
        if (some != null) {
            for (NElement e : some) {
                if (values == null) {
                    values = new ArrayList<>();
                }
                values.addAll(denullList(e));
            }
        }
        return values;
    }

    public static List<NElement> addAll(NElement[] some, List<NElement> values) {
        if (some != null) {
            for (NElement e : some) {
                if (values == null) {
                    values = new ArrayList<>();
                }
                values.addAll(denullList(e));
            }
        }
        return values;
    }

    public static List<NElement> add(NElement e, List<NElement> values) {
        if (values == null) {
            values = new ArrayList<>();
        }
        values.addAll(CoreNElementUtils.denullList(e));
        return values;
    }

    public static List<NElement> addAt(int index, NElement e, List<NElement> values) {
        if (values == null) {
            values = new ArrayList<>();
        }
        while (values.size() < index) {
            values.add(NElement.ofNull());
        }
        values.addAll(index, CoreNElementUtils.denullList(e));
        return values;
    }

    public static List<NElement> setPair(NPairElement entry, List<NElement> values) {
        if (entry != null) {
            if (values == null) {
                values = new ArrayList<>();
                values.add(entry);
                return values;
            }
            for (int i = 0; i < values.size(); i++) {
                NElement nElement = values.get(i);
                if (nElement instanceof NPairElement) {
                    NPairElement p2 = (NPairElement) nElement;
                    if (Objects.equals(p2, entry)) {
                        return values;
                    }
                    NElement k = p2.key();
                    if (Objects.equals(k, entry.key())) {
                        values.set(i, entry);
                        return values;
                    }
                }
            }
            values.add(entry);
        }
        return values;
    }

    public static boolean removeAll(NElement any, List<NElement> values) {
        if (values == null) {
            return false;
        }
        if (any == null) {
            return false;
        }
        any = denullOne(any);
        int count = 0;
        for (int i = values.size() - 1; i >= 0; i--) {
            NElement nElement = values.get(i);
            if (Objects.equals(any, nElement)) {
                values.remove(i);
                count++;
            }
        }
        return count > 0;
    }

    public static boolean remove(NElement any, List<NElement> values) {
        if (values == null) {
            return false;
        }
        if (any != null) {
            for (int i = values.size() - 1; i >= 0; i--) {
                NElement nElement = values.get(i);
                if (Objects.equals(nElement, any)) {
                    values.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean removePair(NElement entryKey, List<NElement> values) {
        if (values == null) {
            return false;
        }
        entryKey = denullOne(entryKey);
        for (int i = values.size() - 1; i >= 0; i--) {
            NElement nElement = values.get(i);
            if (nElement instanceof NPairElement) {
                NElement k = ((NPairElement) nElement).key();
                if (Objects.equals(k, entryKey)) {
                    values.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean removeAllPairs(NElement entryKey, List<NElement> values) {
        if (values == null) {
            return false;
        }
        entryKey = denullOne(entryKey);
        int count = 0;
        for (int i = values.size() - 1; i >= 0; i--) {
            NElement nElement = values.get(i);
            if (nElement instanceof NPairElement) {
                NElement k = ((NPairElement) nElement).key();
                if (Objects.equals(k, entryKey)) {
                    values.remove(i);
                    count++;
                }
            }
        }
        return count > 0;
    }

    public static boolean removePair(String entryKey, List<NElement> values) {
        if (values == null) {
            return false;
        }
        if (entryKey == null) {
            for (int i = values.size() - 1; i >= 0; i--) {
                NElement nElement = values.get(i);
                if (nElement instanceof NPairElement) {
                    NElement k = ((NPairElement) nElement).key();
                    if (k.isNull()) {
                        values.remove(i);
                        return true;
                    }
                }
            }
        } else {
            for (int i = values.size() - 1; i >= 0; i--) {
                NElement nElement = values.get(i);
                if (nElement instanceof NPairElement) {
                    NElement k = ((NPairElement) nElement).key();
                    if ((k.isAnyStringOrName()) && k.asStringValue().orElse("").equals(entryKey)) {
                        values.remove(i);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean removeAllPairs(String entryKey, List<NElement> values) {
        if (values == null) {
            return false;
        }
        int count = 0;
        if (entryKey == null) {
            for (int i = values.size() - 1; i >= 0; i--) {
                NElement nElement = values.get(i);
                if (nElement instanceof NPairElement) {
                    NElement k = ((NPairElement) nElement).key();
                    if (k.isNull()) {
                        values.remove(i);
                        count++;
                    }
                }
            }
        } else {
            for (int i = values.size() - 1; i >= 0; i--) {
                NElement nElement = values.get(i);
                if (nElement instanceof NPairElement) {
                    NElement k = ((NPairElement) nElement).key();
                    if ((k.isAnyStringOrName()) && k.asStringValue().orElse("").equals(entryKey)) {
                        values.remove(i);
                        count++;
                    }
                }
            }
        }
        return count > 0;
    }

    public static NElement denullOne(NElement e) {
        if (e == null) {
            return NElement.ofNull();
        }
        return e;
    }

    public static NPairElement pair(NElement k, NElement v) {
        return new DefaultNPairElement(denullOne(k), denullOne(v));
    }

    public static NPairElement pair(NElement k, boolean v) {
        return new DefaultNPairElement(denullOne(k), NElement.ofBoolean(v));
    }

    public static NPairElement pair(NElement k, int v) {
        return new DefaultNPairElement(denullOne(k), NElement.ofInt(v));
    }


    public static NPairElement pair(String name, NElement value) {
        return pair(NElement.ofNameOrString(name), denullOne(value));
    }

    public static NPairElement pair(String name, boolean value) {
        return pair(NElement.ofNameOrString(name), NElement.ofBoolean(value));
    }

    public static NPairElement pair(String name, int value) {
        return pair(NElement.ofNameOrString(name), NElement.ofInt(value));
    }

    public static NPairElement pair(String name, double value) {
        return pair(NElement.ofNameOrString(name), NElement.ofDouble(value));
    }

    public static NPairElement pair(String name, String value) {
        return pair(NElement.ofNameOrString(name), NElement.ofString(value));
    }

    public static void removeAt(int index, List<NElement> values) {
        values.remove(index);
    }

    public static List<NElement> addMap(Map<NElement, NElement> other, List<NElement> values) {
        if (other != null) {
            for (Map.Entry<NElement, NElement> e : other.entrySet()) {
                values = add(pair(e.getKey(), e.getValue()), values);
            }
        }
        return values;
    }

    public static NElement defragment(NElement any) {
        if (any == null) {
            return NElement.ofNull();
        }
        if (any.isFragment()) {
            NFragmentElement f = any.asFragment().get();
            int s = f.size();
            if (s == 0) {
                return NElement.ofNull();
            }
            if (s == 1) {
                return f.children().get(0);
            }
            return NElement.ofUplet(f.children().toArray(new NElement[0]));
        }
        return any;
    }
}
