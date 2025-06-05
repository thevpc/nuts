package net.thevpc.nuts.runtime.standalone.format.elem.mapper.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.reflect.NReflectProperty;
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
    List<NElementMapperBuilderFieldImpl<T>> allTFields = new ArrayList<>();
    Map<String, NElementMapperBuilderFieldImpl<T>> argFields = new HashMap<>();
    Map<String, NElementMapperBuilderFieldImpl<T>> bodyFields = new HashMap<>();
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

    @Override
    public NElementMapperBuilder<T> configureLenient() {
        this
                .addAllFields()
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
    public NElementMapperBuilder<T> addAllFields() {
        for (NReflectProperty allField : type.getProperties()) {
            addField(allField.getName());
        }
        return this;
    }

    @Override
    public NElementMapperBuilder<T> addFields(Predicate<String> fieldFilter) {
        for (NReflectProperty allField : type.getProperties()) {
            if (fieldFilter == null || fieldFilter.test(allField.getName())) {
                addField(allField.getName());
            }
        }
        return this;
    }

    @Override
    public NElementMapperBuilder<T> addFields(String... names) {
        for (String name : names) {
            addField(name);
        }
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

    @Override
    public NElementMapperBuilderFieldImpl<T> addField(String name) {
        NElementMapperBuilderFieldImpl<T> f = allTFields.stream().filter(x -> x.name.equals(name)).findFirst().orElse(null);
        if (f == null) {
            f = new NElementMapperBuilderFieldImpl<>(name, this);
            allTFields.add(f);
        }
        invalidateBuild();
        return f;
    }

    @Override
    public boolean removeField(String name) {
        NElementMapperBuilderFieldImpl<T> f = allTFields.stream().filter(x -> x.name.equals(name)).findFirst().orElse(null);
        if (f != null) {
            allTFields.remove(f);
            invalidateBuild();
            return true;
        }
        return false;
    }

    @Override
    public NElementMapperBuilder<T> removeFields(String... names) {
        if (names != null) {
            for (String name : names) {
                NElementMapperBuilderFieldImpl<T> f = allTFields.stream().filter(x -> x.name.equals(name)).findFirst().orElse(null);
                if (f != null) {
                    allTFields.remove(f);
                    invalidateBuild();
                }
            }
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

    private void prepareBuilder() {
        if (!built) {
            argFields.clear();
            bodyFields.clear();
            for (NElementMapperBuilderFieldImpl<T> f : allTFields) {
                f.uniformName = uniformName(f.name);
                f.field = null;
                for (NReflectProperty field : type.getProperties()) {
                    String u = uniformName(field.getName());
                    if (u.equals(f.uniformName)) {
                        f.field = field;
                        break;
                    }
                }

                boolean body = f.body || (!f.arg && !f.body);
                boolean arg = f.arg || (!f.arg && !f.body);
                if (body) {
                    bodyFields.put(f.uniformName, f);
                }
                if (arg) {
                    argFields.put(f.uniformName, f);
                }
            }
            built = true;
        }
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
        prepareBuilder();
        return new NElementMapperFromBuilder<>(this);
    }

}
