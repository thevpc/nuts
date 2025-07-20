package net.thevpc.nuts.runtime.standalone.format.elem;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.format.NFormattable;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NMsg;

import java.lang.reflect.*;
import java.time.temporal.Temporal;
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
                    case "net.thevpc.nuts.NDoubleComplex":
                    case "net.thevpc.nuts.NFloatComplex":
                    case "net.thevpc.nuts.NBigComplex":
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
                                || NFormattable.class.isAssignableFrom(cls)
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
                    case "net.thevpc.nuts.NDoubleComplex":
                    case "net.thevpc.nuts.NFloatComplex":
                    case "net.thevpc.nuts.NBigComplex":
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
                                || NFormattable.class.isAssignableFrom(cls)
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
}
