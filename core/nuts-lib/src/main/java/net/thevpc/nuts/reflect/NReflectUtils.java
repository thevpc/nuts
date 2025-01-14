package net.thevpc.nuts.reflect;

import net.thevpc.nuts.util.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class NReflectUtils {
    private NReflectUtils() {
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
}
