package net.thevpc.nuts.reflect;

import net.thevpc.nuts.NBigComplex;
import net.thevpc.nuts.NDoubleComplex;
import net.thevpc.nuts.NFloatComplex;
import net.thevpc.nuts.util.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class NReflectUtils {
    private NReflectUtils() {
    }

    public static boolean isValidIdentifier(String anyType, String extraWordChars) {
        if (anyType == null) {
            return false;
        }
        char[] chars = anyType.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (i == 0) {
                if (c == '_' || c == '-' || c == '.') {
                    return false;
                }
                if (!Character.isJavaIdentifierStart(c)) {
                    if ((extraWordChars == null || extraWordChars.indexOf(c) < 0)) {
                        return false;
                    }
                }
            }
            if (!Character.isJavaIdentifierPart(c)) {
                if ((extraWordChars == null || extraWordChars.indexOf(c) < 0)) {
                    return false;
                }
            }
            if (i == chars.length - 1) {
                if (c == '_' || c == '-' || c == '.') {
                    return false;
                }
            }
        }
        return true;
    }

    public static Object getDefaultValue(Class<?> anyType) {
        NAssert.requireNonNull(anyType, "type");
        switch (anyType.getName()) {
            case "boolean":
                return false;
            case "byte":
                return (byte) 0;
            case "short":
                return (short) 0;
            case "int":
                return (int) 0;
            case "long":
                return 0L;
            case "char":
                return '\0';
            case "float":
                return 0.0f;
            case "double":
                return 0.0;
            case "void":
                return null;
        }
        return null;
    }

    public static NOptional<Class<?>> toBoxedType(Class<?> anyType) {
        if (anyType == null) {
            return NOptional.ofNamedError("no boxed type for null");
        }
        switch (anyType.getName()) {
            case "boolean":
                return NOptional.of(Boolean.class);
            case "byte":
                return NOptional.of(Byte.class);
            case "short":
                return NOptional.of(Short.class);
            case "int":
                return NOptional.of(Integer.class);
            case "long":
                return NOptional.of(Long.class);
            case "char":
                return NOptional.of(Character.class);
            case "float":
                return NOptional.of(Float.class);
            case "double":
                return NOptional.of(Double.class);
            case "void":
                return NOptional.of(Void.class);
        }
        return NOptional.ofNamedError(NMsg.ofC("not a primitive type %s", anyType));
    }

    public static NOptional<Class<?>> toPrimitiveType(Class<?> anyType) {
        if (anyType == null) {
            return NOptional.ofNamedError("no boxed type for null");
        }
        switch (anyType.getName()) {
            case "java.lang.Boolean":
                return NOptional.of(Boolean.TYPE);
            case "java.lang.Byte":
                return NOptional.of(Byte.TYPE);
            case "java.lang.Short":
                return NOptional.of(Short.TYPE);
            case "java.lang.Integer":
                return NOptional.of(Integer.TYPE);
            case "java.lang.Long":
                return NOptional.of(Long.TYPE);
            case "java.lang.Character":
                return NOptional.of(Character.TYPE);
            case "java.lang.Float":
                return NOptional.of(Float.TYPE);
            case "java.lang.Double":
                return NOptional.of(Double.TYPE);
            case "java.lang.Void":
                return NOptional.of(Void.TYPE);
        }
        return NOptional.ofNamedError(NMsg.ofC("not a primitive type %s", anyType));
    }

    public static Set<Class<?>> findAllSuperClassesAndInterfaces(Class<?> clazz) {
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        Set<Class<?>> nextLevel = new LinkedHashSet<Class<?>>();
        nextLevel.add(clazz);
        while (!nextLevel.isEmpty()) {
            classes.addAll(nextLevel);
            Set<Class<?>> thisLevel = new LinkedHashSet<Class<?>>(nextLevel);
            nextLevel.clear();
            for (Class<?> each : thisLevel) {
                Class<?> superClass = each.getSuperclass();
                if (superClass != null && superClass != Object.class) {
                    nextLevel.add(superClass);
                }
                for (Class<?> eachInt : each.getInterfaces()) {
                    nextLevel.add(eachInt);
                }
            }
        }
        return classes;
    }

    public static Class<?> commonAncestor(Class<?>... classes) {
        return commonAncestors(classes).get(0);
    }

    public static List<Class<?>> commonAncestors(Class<?>... classes) {
        if (classes == null || classes.length == 0) {
            ArrayList<Class<?>> a = new ArrayList<>(1);
            a.add(Object.class);
            return a;
        }
        Set<Class<?>> rollingIntersect = null;
        for (int i = 0; i < classes.length; i++) {
            if (classes[i] != null) {
                if (rollingIntersect == null) {
                    rollingIntersect = new LinkedHashSet<>(findAllSuperClassesAndInterfaces(classes[0]));
                } else {
                    rollingIntersect.retainAll(findAllSuperClassesAndInterfaces(classes[i]));
                }
            }
        }
        if (rollingIntersect == null) {
            ArrayList<Class<?>> a = new ArrayList<>(1);
            a.add(Object.class);
            return a;
        }
        return new ArrayList<>(rollingIntersect);
    }

    public static Class<? extends Number> commonNumberType(Class<? extends Number> aa, Class<? extends Number> bb) {
        if (aa == null && bb == null) {
            return Number.class;
        }
        if (aa == null) {
            return bb;
        }
        if (bb == null) {
            return aa;
        }
        if (NBigComplex.class.equals(aa) || NBigComplex.class.equals(bb)) {
            return NBigComplex.class;
        }
        if (NDoubleComplex.class.equals(aa) || NDoubleComplex.class.equals(bb)) {
            if (BigInteger.class.equals(aa) || BigInteger.class.equals(bb)) {
                return NBigComplex.class;
            }
            return NDoubleComplex.class;
        }
        if (NFloatComplex.class.equals(aa) || NFloatComplex.class.equals(bb)) {
            if (BigInteger.class.equals(aa) || BigInteger.class.equals(bb)) {
                return NBigComplex.class;
            }
            if (Long.class.equals(aa) || Long.class.equals(bb)) {
                return NDoubleComplex.class;
            }
            return NFloatComplex.class;
        }
        if (BigDecimal.class.equals(aa) || BigDecimal.class.equals(bb)) {
            return BigDecimal.class;
        }
        if (BigInteger.class.equals(aa) || BigInteger.class.equals(bb)) {
            if (Double.class.equals(aa) || Double.class.equals(bb) || Float.class.equals(aa) || Float.class.equals(bb)) {
                return BigDecimal.class;
            }
            return BigInteger.class;
        }
        if (Double.class.equals(aa) || Double.class.equals(bb)) {
            return Double.class;
        }
        if (Float.class.equals(aa) || Float.class.equals(bb)) {
            if (Long.class.equals(aa) || Long.class.equals(bb)) {
                return Double.class;
            }
            return Float.class;
        }
        if (Long.class.equals(aa) || Long.class.equals(bb)) {
            return Long.class;
        }
        if (Integer.class.equals(aa) || Integer.class.equals(bb)) {
            return Integer.class;
        }
        if (Short.class.equals(aa) || Short.class.equals(bb)) {
            return Short.class;
        }
        if (Byte.class.equals(aa) || Byte.class.equals(bb)) {
            return Byte.class;
        }
        return Number.class;
    }

    public static int compareNumbers(Number a, Number b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        Class<? extends Number> ct = commonNumberType(a.getClass(), b.getClass());
        switch (ct.getName()) {
            case "java.lang.Byte":
            case "java.lang.Short":
            case "java.lang.Integer":
            case "java.lang.Long": {
                return Long.compare(a.longValue(), b.longValue());
            }
            case "java.lang.Float":
            case "java.lang.Double": {
                return Double.compare(a.doubleValue(), b.doubleValue());
            }
            case "java.math.BigInteger": {
                return NLiteral.of(a).asBigInt().get().compareTo(NLiteral.of(b).asBigInt().get());
            }
            case "java.math.BigDecimal": {
                return NLiteral.of(a).asBigDecimal().get().compareTo(NLiteral.of(b).asBigDecimal().get());
            }
            case "net.thevpc.nuts.NFloatComplex": {
                return NLiteral.of(a).asFloatComplex().get().compareTo(NLiteral.of(b).asFloatComplex().get());
            }
            case "net.thevpc.nuts.NDoubleComplex": {
                return NLiteral.of(a).asDoubleComplex().get().compareTo(NLiteral.of(b).asDoubleComplex().get());
            }
            case "net.thevpc.nuts.NBigComplex": {
                return NLiteral.of(a).asBigComplex().get().compareTo(NLiteral.of(b).asBigComplex().get());
            }
        }
        return String.valueOf(a).compareTo(String.valueOf(b));
    }

    public static Number addNumbers(Number a, Number b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        Class<? extends Number> ct = commonNumberType(a.getClass(), b.getClass());
        switch (ct.getName()) {
            case "java.lang.Byte":{
                return NLiteral.of(a).asByte().get()+NLiteral.of(b).asByte().get();
            }
            case "java.lang.Short":{
                return NLiteral.of(a).asShort().get()+NLiteral.of(b).asShort().get();
            }
            case "java.lang.Integer":{
                return NLiteral.of(a).asInt().get()+NLiteral.of(b).asInt().get();
            }
            case "java.lang.Long": {
                return NLiteral.of(a).asLong().get()+NLiteral.of(b).asLong().get();
            }
            case "java.lang.Float":{
                return NLiteral.of(a).asFloat().get()+NLiteral.of(b).asFloat().get();
            }
            case "java.lang.Double": {
                return NLiteral.of(a).asDouble().get()+NLiteral.of(b).asDouble().get();
            }
            case "java.math.BigInteger": {
                return NLiteral.of(a).asBigInt().get().add(NLiteral.of(b).asBigInt().get());
            }
            case "java.math.BigDecimal": {
                return NLiteral.of(a).asBigDecimal().get().add(NLiteral.of(b).asBigDecimal().get());
            }
            case "net.thevpc.nuts.NFloatComplex": {
                return NLiteral.of(a).asFloatComplex().get().add(NLiteral.of(b).asFloatComplex().get());
            }
            case "net.thevpc.nuts.NDoubleComplex": {
                return NLiteral.of(a).asDoubleComplex().get().add(NLiteral.of(b).asDoubleComplex().get());
            }
            case "net.thevpc.nuts.NBigComplex": {
                return NLiteral.of(a).asBigComplex().get().add(NLiteral.of(b).asBigComplex().get());
            }
        }
        return String.valueOf(a).compareTo(String.valueOf(b));
    }
}
