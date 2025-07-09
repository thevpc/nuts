package net.thevpc.nuts.runtime.standalone.format.elem.mapper.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NReflectUtils;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class DefaultNElementMapperBuilder<T> implements NElementMapperBuilder<T> {
    NReflectType type;
    Function<String, String> renamer;
    Predicate<String> paramFieldFieldFilter;
    Predicate<String> bodyFieldNameFilter;
    boolean built = false;
    boolean wrapCollections = true;
    boolean containerIsCollection = false;
    List<NElementMapperBuilderFieldConfigurer<T>> onUnsupportedBody = new ArrayList<>();
    List<NElementMapperBuilderFieldConfigurer<T>> onUnsupportedArg = new ArrayList<>();
    List<NElementMapperBuilderInitializer<T>> postProcess = new ArrayList<>();
    NElementMapperBuilderInstanceFactory<T> onNewInstance;
    Map<Type, Object> defaultValueByType = new HashMap<>();

    public DefaultNElementMapperBuilder(NReflectRepository javaWord, Type type) {
        this.type = javaWord.getType(type);
    }

    public Predicate<String> getParamFieldFieldFilter() {
        return paramFieldFieldFilter;
    }

    public NElementMapperBuilder<T> setParamFieldFieldFilter(Predicate<String> paramFieldFieldFilter) {
        this.paramFieldFieldFilter = paramFieldFieldFilter;
        return this;
    }

    public Predicate<String> getBodyFieldNameFilter() {
        return bodyFieldNameFilter;
    }

    public NElementMapperBuilder<T> setBodyFieldNameFilter(Predicate<String> bodyFieldNameFilter) {
        this.bodyFieldNameFilter = bodyFieldNameFilter;
        return this;
    }

    @Override
    public NElementMapperBuilder<T> configureLenient() {
        this
                .setParamFieldFieldFilter(null)
                .setBodyFieldNameFilter(null)
                .setWrapCollections(true)
        ;
        return this;
    }

    public NElementMapperBuilder<T> setWrapCollections(boolean wrapCollections) {
        this.wrapCollections = wrapCollections;
        invalidateBuild();
        return this;
    }

    public NElementMapperBuilder<T> setContainerIsCollection(boolean value) {
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
    public NElementMapperBuilder<T> setBooleanDefaultTrue() {
        return setTypeDefaultValue(Boolean.class, true);
    }

    @Override
    public NElementMapperBuilder<T> setBooleanDefaultFalse() {
        return setTypeDefaultValue(Boolean.class, false);
    }

    public NElementMapperBuilder<T> setInstanceFactory(NElementMapperBuilderInstanceFactory<T> instanceFactory) {
        this.onNewInstance = instanceFactory;
        return this;
    }

    public NElementMapperBuilder<T> setTypeDefaultValue(Type type, Object defaultValue) {
        defaultValueByType.put(uniformType(type), defaultValue);
        return this;
    }

    @Override
    public NElementMapperBuilder<T> onUnsupportedChild(NElementMapperBuilderFieldConfigurer<T> a) {
        if (a != null) {
            onUnsupportedBody.add(a);
        }
        return this;
    }

    @Override
    public NElementMapperBuilder<T> onUnsupportedParam(NElementMapperBuilderFieldConfigurer<T> a) {
        if (a != null) {
            onUnsupportedArg.add(a);
        }
        return this;
    }

    @Override
    public NElementMapperBuilder<T> onInitializeInstance(NElementMapperBuilderInitializer<T> a) {
        if (a != null) {
            postProcess.add(a);
        }
        return this;
    }

    private void invalidateBuild() {
        this.built = false;
    }

    private String uniformName(String s) {
        s = s.trim();
        if (renamer == null) {
            return s;
        }
        return renamer.apply(s);
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
    public NElementMapper<T> build() {
        return new NElementMapperFromBuilder<>(this);
    }

}
