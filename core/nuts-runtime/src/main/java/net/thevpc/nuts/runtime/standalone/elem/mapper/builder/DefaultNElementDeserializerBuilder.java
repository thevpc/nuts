package net.thevpc.nuts.runtime.standalone.elem.mapper.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NReflectUtils;
import net.thevpc.nuts.util.NNameFormat;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class DefaultNElementDeserializerBuilder<T> implements NElementDeserializerBuilder<T> {
    NReflectType type;
    Function<String, String> fieldNameNormalizer;
    Predicate<String> paramFieldFieldFilter;
    Predicate<String> bodyFieldNameFilter;
    boolean built = false;
    boolean wrapCollections = true;
    boolean containerIsCollection = false;
    List<NElementDeserializerFieldConfigurer<T>> onUnsupportedBody = new ArrayList<>();
    List<NElementDeserializerFieldConfigurer<T>> onUnsupportedArg = new ArrayList<>();
    List<NElementDeserializerInitializer<T>> postProcess = new ArrayList<>();
    NElementDeserializerInstanceFactory<T> onNewInstance;
    Map<Type, Object> defaultValueByType = new HashMap<>();
    Map<String, NElementDeserializerField<T>> preConfiguredFields = new HashMap<>();

    public DefaultNElementDeserializerBuilder(NReflectRepository javaWord, Type type) {
        this.type = javaWord.getType(type);
    }

    public Predicate<String> paramFieldFilter() {
        return paramFieldFieldFilter;
    }

    public NElementDeserializerBuilder<T> paramFieldFilter(Predicate<String> paramFieldFilter) {
        this.paramFieldFieldFilter = paramFieldFilter;
        return this;
    }

    public Predicate<String> childFieldNameFilter() {
        return bodyFieldNameFilter;
    }

    public NElementDeserializerBuilder<T> childFieldNameFilter(Predicate<String> childFieldNameFilter) {
        this.bodyFieldNameFilter = childFieldNameFilter;
        return this;
    }

    @Override
    public NElementDeserializerField<T> field(String name) {
        return preConfiguredFields.computeIfAbsent(name,
                k -> new NElementDeserializerBuilderNElementDeserializerFieldImpl<>(name, this));
    }

    @Override
    public NElementDeserializerBuilder<T> configureLenient() {
        this
                .paramFieldFilter(null)
                .childFieldNameFilter(null)
                .wrapCollections(true)
        ;
        return this;
    }

    @Override
    public NElementDeserializerBuilder<T> fieldNameNormalizer(Function<String, String> normalizer) {
        this.fieldNameNormalizer = normalizer;
        return this;
    }

    @Override
    public NElementDeserializerBuilder<T> fieldNameNormalizer(NNameFormat normalizer) {
        this.fieldNameNormalizer = normalizer==null?null:normalizer::format;
        return this;
    }

    public NElementDeserializerBuilder<T> wrapCollections(boolean wrapCollections) {
        this.wrapCollections = wrapCollections;
        invalidateBuild();
        return this;
    }

    public NElementDeserializerBuilder<T> containerIsCollection(boolean value) {
        this.containerIsCollection = value;
        invalidateBuild();
        return this;
    }

    private Type uniformType(Type type) {
        if (type instanceof Class) {
            Class<?> c = (Class<?>) type;
            return NReflectUtils.toBoxedType(c).orElse(c);
        }
        return type;
    }

    public boolean hasDefaultValueByType(Type type) {
        return defaultValueByType.containsKey(uniformType(type));
    }

    public Object getDefaultValueByType(Type type) {
        return defaultValueByType.get(uniformType(type));
    }

    @Override
    public NElementDeserializerBuilder<T> booleanDefaultTrue() {
        return typeDefaultValue(Boolean.class, true);
    }

    @Override
    public NElementDeserializerBuilder<T> booleanDefaultFalse() {
        return typeDefaultValue(Boolean.class, false);
    }

    public NElementDeserializerBuilder<T> instanceFactory(NElementDeserializerInstanceFactory<T> instanceFactory) {
        this.onNewInstance = instanceFactory;
        return this;
    }

    public NElementDeserializerBuilder<T> typeDefaultValue(Type type, Object defaultValue) {
        defaultValueByType.put(uniformType(type), defaultValue);
        return this;
    }

    @Override
    public NElementDeserializerBuilder<T> onUnsupportedChild(NElementDeserializerFieldConfigurer<T> a) {
        if (a != null) {
            onUnsupportedBody.add(a);
        }
        return this;
    }

    @Override
    public NElementDeserializerBuilder<T> onUnsupportedParam(NElementDeserializerFieldConfigurer<T> a) {
        if (a != null) {
            onUnsupportedArg.add(a);
        }
        return this;
    }

    @Override
    public NElementDeserializerBuilder<T> onInitializeInstance(NElementDeserializerInitializer<T> a) {
        if (a != null) {
            postProcess.add(a);
        }
        return this;
    }

    private void invalidateBuild() {
        this.built = false;
    }

    public boolean isCollectionType0(Type raw) {
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
    public NElementDeserializer<T> build() {
        return new NElementMapperFromBuilder<>(this);
    }

}
