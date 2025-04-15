package net.thevpc.nuts.runtime.standalone.tson.impl.marshall;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class ClassPropertiesRegistry {
    public static final ClassPropertiesRegistry DEFAULT=new ClassPropertiesRegistry();
    private Map<Class, ClassInfo> classes = new HashMap<>();

    public synchronized ClassInfo getClassInfo(Class clazz) {
        ClassInfo t = classes.get(clazz);
        if (t != null) {
            return t;
        }
        Class s = clazz.getSuperclass();
        ClassInfo p = null;
        if (s != null) {
            p = getClassInfo(s);
        }
        ClassInfo ci = new ClassInfo(clazz);
        ci.parent = p;
        for (Field declaredField : clazz.getDeclaredFields()) {
            int m = declaredField.getModifiers();
            if (
                    (m & Modifier.STATIC) == 0 &&
                            (m & Modifier.TRANSIENT) == 0
            ) {
                ci.infos.put(declaredField.getName(), new FieldTypeProperty(declaredField));
            }
        }
        classes.put(clazz, ci);
        return ci;
    }

    public class ClassInfo {
        private Map<String, TypeProperty> infos = new LinkedHashMap<>();
        private ClassInfo parent;
        private Class cls;

        public ClassInfo(Class cls) {
            this.cls = cls;
        }

        public String name(){
            return cls.getName();
        }

        public Class getClazz() {
            return cls;
        }

        public <T> T newInstance() {
            try {
                Constructor c = cls.getConstructor();
                if (c != null) {
                    c.setAccessible(true);
                    T v = (T) c.newInstance();
                    return v;
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to instantiate " + cls, e);
            }
            throw new IllegalArgumentException("Unable to instantiate " + cls);
        }

        public TypeProperty getProperty(String name, boolean includeParent) {
            TypeProperty p = infos.get(name);
            if (p != null) {
                return p;
            }
            if (includeParent && parent != null) {
                return parent.getProperty(name, includeParent);
            }
            return null;
        }

        public Collection<TypeProperty> getProperties(boolean includeParent) {
            if (!includeParent || parent == null) {
                return infos.values();
            }
            ArrayList<TypeProperty> a = new ArrayList<>(infos.values());
            a.addAll(parent.getProperties(true));
            return a;
        }

        public ClassInfo getParent() {
            return parent;
        }
    }

    public interface TypeProperty {
        String name();

        Class type();

        Object get(Object o);

        void set(Object o, Object val);
    }

    public class FieldTypeProperty implements TypeProperty {
        private Field field;

        public FieldTypeProperty(Field field) {
            this.field = field;
            this.field.setAccessible(true);
        }

        @Override
        public String name() {
            return field.getName();
        }

        @Override
        public Class type() {
            return field.getType();
        }

        @Override
        public Object get(Object o) {
            try {
                return field.get(o);
            } catch (IllegalAccessException e) {
                throw new UnsupportedOperationException(e);
            }
        }

        @Override
        public void set(Object o, Object val) {
            try {
                field.set(o, val);
            } catch (IllegalAccessException e) {
                throw new UnsupportedOperationException(e);
            }
        }
    }
}
