package net.thevpc.nuts.runtime.standalone.elem.mapper.builder;

import net.thevpc.nuts.elem.NElementMapperBuilder;
import net.thevpc.nuts.reflect.NReflectProperty;

import java.lang.reflect.Type;
import java.util.Collection;

public class NElementMapperBuilderFieldImpl<T> implements NElementMapperBuilder.FieldConfig<T> {
    DefaultNElementMapperBuilder<T> parent;
    String uniformName;
    String name;
    boolean arg;
    boolean body;
    NReflectProperty field;
    Boolean wrapCollections = true;
    Boolean containerIsCollection = false;
    Boolean useDefaultWhenMissingValue;
    Object valueWhenMissing;

    public NElementMapperBuilderFieldImpl(String name, DefaultNElementMapperBuilder<T> parent) {
        this.name = name;
        this.parent = parent;
    }

    @Override
    public NElementMapperBuilder.FieldConfig<T> setBooleanDefaultTrue() {
        if (isBooleanType()) {
            setDefaultValue(Boolean.TRUE);
            return this;
        } else {
            throw new IllegalArgumentException("expected boolean");
        }
    }

    @Override
    public NElementMapperBuilder.FieldConfig<T> setBooleanDefaultFalse() {
        if (isBooleanType()) {
            setDefaultValue(Boolean.FALSE);
            return this;
        } else {
            throw new IllegalArgumentException("expected boolean");
        }
    }

    @Override
    public NElementMapperBuilder.FieldConfig<T> setDefaultValue(Object valueWhenMissing) {
        this.useDefaultWhenMissingValue = true;
        this.valueWhenMissing = valueWhenMissing;
        return this;
    }

    Object getValueWhenMissing() {
        if (valueWhenMissing != null) {
            return valueWhenMissing;
        }
        Type raw = field.getDeclaringType().getJavaType();
        return parent.getDefaultValueByType(raw);
    }

    boolean isUseDefaultWhenMissingValue() {
        if (useDefaultWhenMissingValue != null) {
            return useDefaultWhenMissingValue;
        }
        Type raw = field.getDeclaringType().getJavaType();
        return parent.hasDefaultValueByType(raw);
    }

    public boolean isBooleanType() {
        Type raw = field.getDeclaringType().getJavaType();
        if (raw instanceof Class) {
            Class<?> cls = (Class<?>) raw;
            if (cls.equals(Boolean.class) || cls.equals(Boolean.TYPE)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCollectionType() {
        Type raw = field.getDeclaringType().getJavaType();
        if (raw instanceof Class) {
            Class<?> cls = (Class<?>) raw;
            if (cls.isArray()) {
                return true;
            }
            if (Collection.class.isAssignableFrom(cls)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NElementMapperBuilder.FieldConfig<T> setWrapCollections(Boolean value) {
        this.wrapCollections = value;
        return this;
    }

    @Override
    public NElementMapperBuilder.FieldConfig<T> setContainerIsCollection(Boolean value) {
        this.containerIsCollection = value;
        return this;
    }


    public boolean isWrapCollections() {
        if (wrapCollections != null) {
            return wrapCollections;
        }
        return parent.wrapCollections;
    }

    public boolean isContainerIsCollection() {
        if (containerIsCollection != null) {
            return containerIsCollection;
        }
        return parent.containerIsCollection;
    }

    @Override
    public NElementMapperBuilder.FieldConfig<T> setParam(boolean param) {
        this.arg = param;
        return this;
    }

    @Override
    public NElementMapperBuilder.FieldConfig<T> setChild(boolean child) {
        this.body = child;
        return this;
    }

    @Override
    public NElementMapperBuilder<T> end() {
        return parent;
    }
}
