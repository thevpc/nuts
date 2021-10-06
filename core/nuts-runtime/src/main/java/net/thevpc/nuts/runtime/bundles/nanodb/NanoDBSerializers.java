package net.thevpc.nuts.runtime.bundles.nanodb;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class NanoDBSerializers {
    private Map<Class, NanoDBSerializer> config = new HashMap<>();
    private Map<ClassWithNullable, NanoDBSerializer> cache = new HashMap<>();

    public NanoDBSerializers() {
    }

    public NanoDBSerializer ofBean(Class clz) {
        return ofBean(clz, true);
    }

    public NanoDBSerializer ofBean(Class clz, boolean nullable) {
        if (nullable) {
            return new NanoDBSerializerForNullable(ofBean(clz, false));
        }
        return new NanoDBSerializerForBean(clz, this);
    }

    public NanoDBSerializer of(Class clz, boolean nullable) {
        NanoDBSerializer u = findSerializer(clz, nullable);
        if (u != null) {
            return u;
        }
        ClassWithNullable id = new ClassWithNullable(clz, nullable);
        throw new IllegalArgumentException("missing serializer for " + id);
    }

    public void setSerializer(Class clz, Supplier<NanoDBSerializer> nonNullSerializer) {
        NanoDBSerializer old = config.get(clz);
        if (old != null) {
            return;
        }
        if (nonNullSerializer == null) {
            return;
        }
        NanoDBSerializer nonNullSerInstance = nonNullSerializer.get();
        if (nonNullSerInstance != null) {
            config.put(clz, nonNullSerInstance);
        }
    }

    public void setSerializer(Class clz, NanoDBSerializer ser) {
        NanoDBSerializer old = config.get(clz);
        if (old != null) {
            invalidateCache(clz);
        }
        if (ser == null) {
            if (old != null) {
                config.remove(clz);
            }
            return;
        }
        config.put(clz, ser);
    }

    private void invalidateCache(Class clz) {
        for (boolean nullable : new boolean[]{false, true}) {
            cache.remove(new ClassWithNullable(clz, nullable));
        }
    }

    public NanoDBSerializer findSerializer(Class clz) {
        return findSerializer(clz, !clz.isPrimitive());
    }

    public NanoDBSerializer findSerializer(Class clz, boolean nullable) {
        ClassWithNullable id = new ClassWithNullable(clz, nullable);
        NanoDBSerializer t = cache.get(id);
        if (t != null) {
            return t;
        }
        t = config.get(clz);
        if (t == null) {
            t = findDefaultSerializer(id);
        }
        if (nullable) {
            if (t != null) {
                t = new NanoDBSerializerForNullable(t);
                cache.put(id, t);
            }
        } else {
            if (t != null) {
                cache.put(id, t);
            }
        }
        return t;
    }

    private NanoDBSerializer findDefaultSerializer(ClassWithNullable id) {
        switch (id.getCls().getName()) {
            case "int":
            case "java.lang.Integer": {
                return new NanoDBSerializerForInteger();
            }
            case "long":
            case "java.lang.Long": {
                return new NanoDBSerializerForLong();
            }
            case "double":
            case "java.lang.Double": {
                return new NanoDBSerializerForDouble();
            }
            case "boolean":
            case "java.lang.boolean": {
                return new NanoDBSerializerForBoolean();
            }
            case "java.lang.String": {
                return new NanoDBSerializerForString();
            }
        }
        return null;
    }


    private static class ClassWithNullable {
        private final Class cls;
        private final boolean nullable;

        public ClassWithNullable(Class cls, boolean nullable) {
            this.cls = cls;
            this.nullable = nullable;
        }

        public Class getCls() {
            return cls;
        }

        public boolean isNullable() {
            return nullable;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClassWithNullable that = (ClassWithNullable) o;
            return nullable == that.nullable && Objects.equals(cls, that.cls);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cls, nullable);
        }

        @Override
        public String toString() {
            if (cls.isPrimitive()) {
                if (nullable) {
                    return "nullable-" + cls.getName();
                }
                return cls.getName();
            } else {
                if (!nullable) {
                    return "non-nullable-" + cls.getName();
                }
                return cls.getName();
            }
        }
    }

}
