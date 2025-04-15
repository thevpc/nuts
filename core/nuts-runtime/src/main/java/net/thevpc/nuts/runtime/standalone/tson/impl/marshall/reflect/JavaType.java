package net.thevpc.nuts.runtime.standalone.tson.impl.marshall.reflect;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class JavaType {
    private java.lang.reflect.Type jType;
    private JavaWord javaWord;
    private Map<String, JavaField> fields = new HashMap<>();
    private Map<String, JavaField> allFields = new HashMap<>();

    public JavaType(java.lang.reflect.Type jType) {
        this.jType = jType;
    }

    public void init(JavaWord javaWord) {
        this.javaWord = javaWord;
        if (jType instanceof Class) {
            Class<?> cc = (Class<?>) jType;
            Field[] declaredFields = cc.getDeclaredFields();
            for (Field field : declaredFields) {
                fields.put(field.getName(),new JavaField(field, this));
            }
            allFields.putAll(fields);
            cc=cc.getSuperclass();
            while (cc != null) {
                for (JavaField field : getJavaWord().of(cc).getFields()) {
                    if(!allFields.containsKey(field.getName())) {
                        allFields.put(field.getName(),field);
                    }
                }
                cc=cc.getSuperclass();
            }
        } else {
            throw new IllegalArgumentException("not supported yet");
        }
    }

    public JavaField[] getAllFields() {
        return allFields.values().toArray(new JavaField[0]);
    }

    public JavaField[] getFields() {
        return fields.values().toArray(new JavaField[0]);
    }

    public JavaWord getJavaWord() {
        return javaWord;
    }

    public Object newInstance() {
        try {
            return ((Class)jType).newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public java.lang.reflect.Type raw() {
        return jType;
    }
}
