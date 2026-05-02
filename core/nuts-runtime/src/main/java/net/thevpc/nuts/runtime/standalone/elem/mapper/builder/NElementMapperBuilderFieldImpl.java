package net.thevpc.nuts.runtime.standalone.elem.mapper.builder;

import net.thevpc.nuts.elem.NElementDeserializerBuilder;
import net.thevpc.nuts.reflect.NReflectProperty;
import net.thevpc.nuts.util.NStringUtils;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class NElementMapperBuilderFieldImpl<T> implements NElementDeserializerBuilder.FieldConfig<T> {
    DefaultNElementDeserializerBuilder<T> parent;
    String uniformName;
    String name;
    boolean arg;
    boolean body;
    NReflectProperty field;
    Boolean wrapCollections = true;
    Boolean containerIsCollection = false;
    Boolean useDefaultWhenMissingValue;
    Object valueWhenMissing;
    boolean ignored;
    Type typeOverride;
    Set<String> aliases;

    public NElementMapperBuilderFieldImpl(String name, DefaultNElementDeserializerBuilder<T> parent) {
        this.name = name;
        this.parent = parent;
    }

    public NElementMapperBuilderFieldImpl(NElementMapperBuilderFieldImpl other) {
        this.name = other.name;
        this.parent = other.parent;
        this.uniformName = other.uniformName;
        this.arg = other.arg;
        this.body = other.body;
        this.field = other.field;
        this.wrapCollections = other.wrapCollections;
        this.containerIsCollection = other.containerIsCollection;
        this.useDefaultWhenMissingValue = other.useDefaultWhenMissingValue;
        this.valueWhenMissing = other.valueWhenMissing;
        this.ignored = other.ignored;
        this.typeOverride = other.typeOverride;
        this.aliases = other.aliases == null ? null : new HashSet<>(other.aliases);
    }

    public NElementMapperBuilderFieldImpl<T> copy() {
        return new NElementMapperBuilderFieldImpl<>(this);
    }

    public boolean isIgnored() {
        return ignored;
    }

    @Override
    public NElementDeserializerBuilder.FieldConfig<T> setAlias(String... aliases) {
        this.aliases = new HashSet<>();
        if (aliases != null) {
            for (String alias : aliases) {
                String a = NStringUtils.trimToNull(alias);
                if (a != null) {
                    this.aliases.add(a);
                }
            }
        }
        return this;
    }

    @Override
    public NElementDeserializerBuilder.FieldConfig<T> setType(Type type) {
        typeOverride = type;
        return this;
    }

    @Override
    public NElementDeserializerBuilder.FieldConfig<T> ignore() {
        this.ignored = true;
        return this;
    }

    @Override
    public NElementDeserializerBuilder.FieldConfig<T> setBooleanDefaultTrue() {
        if (isBooleanType()) {
            setDefaultValue(Boolean.TRUE);
            return this;
        } else {
            throw new IllegalArgumentException("expected boolean");
        }
    }

    @Override
    public NElementDeserializerBuilder.FieldConfig<T> setBooleanDefaultFalse() {
        if (isBooleanType()) {
            setDefaultValue(Boolean.FALSE);
            return this;
        } else {
            throw new IllegalArgumentException("expected boolean");
        }
    }

    @Override
    public NElementDeserializerBuilder.FieldConfig<T> setDefaultValue(Object valueWhenMissing) {
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
            return cls.equals(Boolean.class) || cls.equals(Boolean.TYPE);
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
            return Collection.class.isAssignableFrom(cls);
        }
        return false;
    }

    @Override
    public NElementDeserializerBuilder.FieldConfig<T> setWrapCollections(Boolean value) {
        this.wrapCollections = value;
        return this;
    }

    @Override
    public NElementDeserializerBuilder.FieldConfig<T> setContainerIsCollection(Boolean value) {
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
    public NElementDeserializerBuilder.FieldConfig<T> setParam(boolean param) {
        this.arg = param;
        return this;
    }

    @Override
    public NElementDeserializerBuilder.FieldConfig<T> setChild(boolean child) {
        this.body = child;
        return this;
    }

    @Override
    public NElementDeserializerBuilder<T> end() {
        return parent;
    }
}
