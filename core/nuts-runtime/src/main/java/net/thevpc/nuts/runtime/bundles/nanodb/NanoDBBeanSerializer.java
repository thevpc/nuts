package net.thevpc.nuts.runtime.bundles.nanodb;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

public class NanoDBBeanSerializer {

    private static <T> LinkedHashMap<String, FieldInfo> buildFields(Class<T> type, NanoDBSerializers serializers){
        LinkedHashMap<String, FieldInfo> fields=new LinkedHashMap<>();
        for (Field declaredField : type.getDeclaredFields()) {
            int m = declaredField.getModifiers();
            if (!Modifier.isFinal(m) && !Modifier.isStatic(m) && !Modifier.isTransient(m)) {
                declaredField.setAccessible(true);
                fields.put(declaredField.getName(),
                        new FieldInfo(
                                declaredField.getName(),
                                declaredField,
                                serializers.of(declaredField.getType(), true)
                        )
                );
            }
        }
        return fields;
    }
    private static <T> void writeNonNullHelper(T obj,Class<T> supportedType, NanoDBOutputStream out, Map<String, FieldInfo> fields) {
        for (FieldInfo value : fields.values()) {
            Object u = null;
            try {
                u = value.field.get(obj);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }
            value.ser.write(u, out);
        }
    }

    private static  <T> T readNonNullHelper(NanoDBInputStream in,Class<T> supportedType, Map<String, FieldInfo> fields) {
        try {
            T newInstance = supportedType.getConstructor().newInstance();
            for (FieldInfo value : fields.values()) {
                try {
                    value.field.set(newInstance, value.ser.read(in));
                } catch (Exception ex) {
                    throw new IllegalArgumentException("error loading field " + supportedType.getSimpleName() + "." + value.name + ": " + ex.getMessage(), ex);
                }
            }
            return newInstance;
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static class NonNull<T> extends NanoDBNonNullSerializer<T> {
        private LinkedHashMap<String, FieldInfo> fields;
        private NanoDBSerializers serializers;
        public NonNull(Class<T> type, NanoDBSerializers serializers) {
            super(type);
            this.serializers = serializers;
            this.fields = buildFields(type, serializers);

        }

        public void write(T obj, NanoDBOutputStream out) {
            writeNonNullHelper(obj,getSupportedType(), out, fields);
        }

        public T read(NanoDBInputStream in) {
            return readNonNullHelper(in,getSupportedType(), fields);
        }
    }

    public static class Null<T> extends NanoDBNullSerializer<T> {
        private LinkedHashMap<String, FieldInfo> fields;
        private NanoDBSerializers serializers;
        public Null(Class<T> type, NanoDBSerializers serializers) {
            super(type);
            this.serializers = serializers;
            this.fields = buildFields(type, serializers);

        }

        public void writeNonNull(T obj, NanoDBOutputStream out) {
            writeNonNullHelper(obj,getSupportedType(), out, fields);
        }

        public T readNonNull(NanoDBInputStream in) {
            return readNonNullHelper(in,getSupportedType(), fields);
        }
    }

    private static class FieldInfo {
        String name;
        Field field;
        NanoDBSerializer ser;

        public FieldInfo(String name, Field field, NanoDBSerializer ser) {
            this.name = name;
            this.field = field;
            this.ser = ser;
        }
    }

}
