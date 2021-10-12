package net.thevpc.nuts.runtime.bundles.nanodb;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class NanoDBSerializerForBean<T> extends NanoDBNonNullSerializer<T>{

    private LinkedHashMap<String, FieldInfo> fields;
    private NanoDBSerializers serializers;
    private Set<String> acceptedFields;
    public NanoDBSerializerForBean(Class<T> type, NanoDBSerializers serializers,Set<String> acceptedFields) {
        super(type);
        this.serializers = serializers;
        this.acceptedFields = acceptedFields==null? Collections.emptySet():acceptedFields;
        this.fields = buildFields(type, acceptedFields,serializers);
    }

    public void write(T obj, NanoDBOutputStream out) {
        writeNonNullHelper(obj,getSupportedType(), out, fields);
    }

    public T read(NanoDBInputStream in) {
        return readNonNullHelper(in,getSupportedType(), fields);
    }

    private static <T> LinkedHashMap<String, FieldInfo> buildFields(Class<T> type, Set<String> acceptedFields,NanoDBSerializers serializers){
        acceptedFields = acceptedFields==null? Collections.emptySet():acceptedFields;
        Class c=type;
        LinkedHashMap<String, FieldInfo> fields = new LinkedHashMap<>();
        while(c!=null) {
            for (Field declaredField : c.getDeclaredFields()) {
                String name = declaredField.getName();
                if(acceptedFields.isEmpty() || acceptedFields.contains(name)) {
                    int m = declaredField.getModifiers();
                    if (!Modifier.isFinal(m) && !Modifier.isStatic(m) && !Modifier.isTransient(m)) {
                        declaredField.setAccessible(true);
                        fields.put(name,
                                new FieldInfo(
                                        name,
                                        declaredField,
                                        serializers.of(declaredField.getType(), true)
                                )
                        );
                    }
                }
            }
            c=c.getDeclaringClass();
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
