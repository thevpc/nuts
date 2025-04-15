package net.thevpc.nuts.runtime.standalone.tson.impl.marshall.reflect;

import java.lang.reflect.Field;

public class JavaField {

    private Field field;
    private JavaType type;
    private boolean enabled;

    public JavaField(Field field, JavaType type) {
        this.field = field;
        this.type = type;
    }

    public String getName() {
        return field.getName();
    }

    public JavaType getType() {
        return getJavaWord().of(field.getType());
    }

    private JavaWord getJavaWord() {
        return type.getJavaWord();
    }

    public void set(Object instance, Object fieldValue) {
        if (!enabled) {
            this.field.setAccessible(true);
            enabled = true;
        }
        try {
            field.set(instance, fieldValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
