package net.thevpc.nuts.reflect;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NReflectUtils {
    private static final Map<Class<?>, Object> DEFAULTS_CACHE = new ConcurrentHashMap<>();

    private NReflectUtils() {
    }

    /**
     * Resolves the default value for a class annotated with @DefaultsTo.
     * Supports:
     * - Enum constants
     * - Static fields
     * - Static no-arg methods
     * Returns null if no @DefaultsTo is present or value cannot be resolved.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDefaultValue(Class<T> type) {
        if (type == null) return null;
        Object d = getJavaDefaultValue(type);
        if (d != null) {
            return (T) d;
        }

        // Check cache first
        if (DEFAULTS_CACHE.containsKey(type)) {
            return (T) DEFAULTS_CACHE.get(type);
        }

        NDefaultsTo annotation = type.getAnnotation(NDefaultsTo.class);
        if (annotation != null) {
            String valueName = annotation.value();
            Object resolved = null;
            try {
                // Enum constant
                if (type.isEnum()) {
                    resolved = Enum.valueOf((Class<Enum>) type.asSubclass(Enum.class), valueName);
                } else {
                    // Static field
                    try {
                        java.lang.reflect.Field field = type.getField(valueName);
                        if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                            resolved = field.get(null);
                        }
                    } catch (NoSuchFieldException ignored) {
                    }

                    // Static method
                    if (resolved == null) {
                        try {
                            java.lang.reflect.Method method = type.getMethod(valueName);
                            if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                                resolved = method.invoke(null);
                            }
                        } catch (NoSuchMethodException ignored) {
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to resolve @DefaultsTo for " + type.getName(), e);
            }

            // Cache result (even nulls)
            DEFAULTS_CACHE.put(type, resolved);
            return (T) resolved;
        }

        // Cache null if no annotation
        DEFAULTS_CACHE.put(type, null);
        return null;
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

    public static boolean isPrimitiveOrBoxed(Class<?> anyType) {
        return isPrimitiveOrBoxed(anyType, true);
    }

    public static boolean isPrimitiveOrBoxed(Class<?> anyType, boolean includeVoid) {
        if (anyType == null) {
            return false;
        }
        switch (anyType.getName()) {
            case "boolean":
            case "byte":
            case "short":
            case "int":
            case "long":
            case "char":
            case "float":
            case "double":
            case "void":
            case "java.lang.Boolean":
            case "java.lang.Byte":
            case "java.lang.Short":
            case "java.lang.Integer":
            case "java.lang.Long":
            case "java.lang.Character":
            case "java.lang.Float":
            case "java.lang.Double":
            case "java.lang.Void":
                return true;
        }
        return false;
    }

    public static Object getJavaDefaultValue(Class<?> anyType) {
        NAssert.requireNamedNonNull(anyType, "type");
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

}
