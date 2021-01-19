package net.thevpc.nuts.runtime.bundles.nanodb;

import java.lang.reflect.Field;

public class NanoDBFieldIndexDefinition<T> extends NanoDBDefaultIndexDefinition<T> {
    private final Field declaredField;
    public NanoDBFieldIndexDefinition(Field declaredField) {
        super(declaredField.getName(), (Class)declaredField.getType(), !declaredField.getType().isPrimitive(),
                o -> {
                    try {
                        return declaredField.get(o);
                    } catch (IllegalAccessException e) {
                        throw new IllegalArgumentException(e);
                    }
                });
        this.declaredField = declaredField;
        declaredField.setAccessible(true);
    }

    public Field getDeclaredField() {
        return declaredField;
    }
}
