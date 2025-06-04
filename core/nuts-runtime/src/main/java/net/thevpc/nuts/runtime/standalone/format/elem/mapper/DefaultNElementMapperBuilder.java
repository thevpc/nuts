package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.reflect.NReflectProperty;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.reflect.NReflectType;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

public class DefaultNElementMapperBuilder<T> implements NElementMapperBuilder<T> {
    NReflectType type;
    Function<String, String> renamer;
    List<TFieldImpl<T>> allTFields = new ArrayList<>();
    Map<String, TFieldImpl<T>> argFields = new HashMap<>();
    Map<String, TFieldImpl<T>> bodyFields = new HashMap<>();
    boolean built = false;
    boolean wrapCollections = true;
    boolean containerIsCollection = false;
    List<MissingFieldConfigurer<T>> onUnsupportedBody = new ArrayList<>();
    List<MissingFieldConfigurer<T>> onUnsupportedArg = new ArrayList<>();
    List<InstanceConfigurer<T>> postProcess = new ArrayList<>();
    InstanceFactory<T> onNewInstance;
    Map<Type, Object> defaultValueByType = new HashMap<>();

    public DefaultNElementMapperBuilder(NReflectRepository javaWord, Type type) {
        this.type = javaWord.getType(type);
    }

    @Override
    public NElementMapperBuilder<T> configureLenient() {
        this
                .addAllFields()
                .setTrueDefault()
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
            if (c.isPrimitive()) {
                switch (c.getName()) {
                    case "boolean":
                        return Boolean.class;
                    case "byte":
                        return Byte.class;
                    case "short":
                        return Short.class;
                    case "int":
                        return Integer.class;
                    case "long":
                        return Long.class;
                    case "float":
                        return Float.class;
                    case "double":
                        return Double.class;
                    case "char":
                        return Character.class;
                }
            }
        }
        return type;
    }

    public boolean hasDefaultValueByType(Type type) {
        return defaultValueByType.containsKey(uniformType(type));
    }

    public Object getDefaultValueByType(Type type) {
        return defaultValueByType.get(uniformType(type));
    }

    public static class TFieldImpl<T> implements FieldConfig<T> {
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

        public TFieldImpl(String name, DefaultNElementMapperBuilder<T> parent) {
            this.name = name;
            this.parent = parent;
        }

        @Override
        public FieldConfig<T> setTrueDefault() {
            if (isBooleanType()) {
                setDefaultValue(Boolean.TRUE);
                return this;
            } else {
                throw new IllegalArgumentException("expected boolean");
            }
        }

        @Override
        public FieldConfig<T> setDefaultValue(Object valueWhenMissing) {
            this.useDefaultWhenMissingValue = true;
            this.valueWhenMissing = valueWhenMissing;
            return this;
        }

        private Object getValueWhenMissing() {
            if (valueWhenMissing != null) {
                return valueWhenMissing;
            }
            Type raw = field.getDeclaringType().getJavaType();
            return parent.getDefaultValueByType(raw);
        }

        private boolean isUseDefaultWhenMissingValue() {
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
        public FieldConfig<T> setWrapCollections(Boolean value) {
            this.wrapCollections = value;
            return this;
        }

        @Override
        public FieldConfig<T> setContainerIsCollection(Boolean value) {
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
        public FieldConfig<T> setArg(boolean arg) {
            this.arg = arg;
            return this;
        }

        @Override
        public FieldConfig<T> setBody(boolean body) {
            this.body = body;
            return this;
        }

        @Override
        public NElementMapperBuilder<T> end() {
            return parent;
        }
    }

    @Override
    public NElementMapperBuilder<T> setTrueDefault() {
        return setDefaultValueByType(Boolean.class, true);
    }

    public NElementMapperBuilder<T> setInstanceFactory(InstanceFactory<T> instanceFactory) {
        this.onNewInstance = instanceFactory;
        return this;
    }

    public NElementMapperBuilder<T> setDefaultValueByType(Type type, Object defaultValue) {
        defaultValueByType.put(uniformType(type), defaultValue);
        return this;
    }

    @Override
    public NElementMapperBuilder<T> addAllFields() {
        for (NReflectProperty allField : type.getProperties()) {
            addField(allField.getName());
        }
        invalidateBuild();
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
    public NElementMapperBuilder<T> onUnsupportedBody(MissingFieldConfigurer<T> a) {
        if (a != null) {
            onUnsupportedBody.add(a);
        }
        return this;
    }

    @Override
    public NElementMapperBuilder<T> onUnsupportedArg(MissingFieldConfigurer<T> a) {
        if (a != null) {
            onUnsupportedArg.add(a);
        }
        return this;
    }

    @Override
    public NElementMapperBuilder<T> postProcess(InstanceConfigurer<T> a) {
        if (a != null) {
            postProcess.add(a);
        }
        return this;
    }

    @Override
    public TFieldImpl<T> addField(String name) {
        TFieldImpl<T> f = allTFields.stream().filter(x -> x.name.equals(name)).findFirst().orElse(null);
        if (f == null) {
            f = new TFieldImpl<>(name, this);
            allTFields.add(f);
        }
        invalidateBuild();
        return f;
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
            for (TFieldImpl<T> f : allTFields) {
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
        return new MyNElementMapper<>(this);
    }

    private static class MyNElementMapper<T> implements NElementMapper<T> {
        private DefaultNElementMapperBuilder<T> builder;
        InstanceFactory<T> onNewInstance;
        NReflectType type;
        List<InstanceConfigurer<T>> postProcess = new ArrayList<>();


        Function<String, String> renamer;
//        List<TFieldImpl<T>> allTFields = new ArrayList<>();
        Map<String, TFieldImpl<T>> argFields = new HashMap<>();
        Map<String, TFieldImpl<T>> bodyFields = new HashMap<>();
//        boolean built = false;
//        boolean wrapCollections = true;
//        boolean containerIsCollection = false;
        List<MissingFieldConfigurer<T>> onUnsupportedBody = new ArrayList<>();
        List<MissingFieldConfigurer<T>> onUnsupportedArg = new ArrayList<>();
//        Map<Type, Object> defaultValueByType = new HashMap<>();

        public MyNElementMapper(DefaultNElementMapperBuilder<T> builder) {
            this.onNewInstance = builder.onNewInstance;
            this.type = builder.type;
            this.postProcess.addAll(builder.postProcess);
            this.argFields.putAll(builder.argFields);
            this.bodyFields.putAll(builder.bodyFields);
            this.onUnsupportedBody.addAll(builder.onUnsupportedBody);
            this.onUnsupportedArg.addAll(builder.onUnsupportedArg);
            this.renamer=builder.renamer;
        }

        @Override
        public Object destruct(T src, Type typeOfSrc, NElementFactoryContext context) {
            return context.defaultDestruct(src, typeOfSrc);
        }

        @Override
        public NElement createElement(T src, Type typeOfSrc, NElementFactoryContext context) {
            return context.defaultCreateElement(src, typeOfSrc);
        }

        @Override
        public T createObject(NElement element, Type to, NElementFactoryContext context) {
            NListContainerElement container = element.toListContainer().get();
            List<NElement> args = container.isParametrized() ? container.asParametrizedContainer().get().params().orNull() : null;
            T instance = null;
            if (onNewInstance != null) {
                instance = onNewInstance.newInstance(new FactoryConfigurerContext<T>() {
                    @Override
                    public NElement element() {
                        return element;
                    }

                    @Override
                    public Class<T> to() {
                        return (Class<T>) to;
                    }

                    @Override
                    public <T1> NElement elem(T1 any) {
                        return context.createElement(any);
                    }

                    @Override
                    public <T1> T1 obj(NElement element, Class<T1> clazz) {
                        return context.createObject(element, clazz);
                    }
                });
            }
            if (instance == null) {
                Type rtype = type.getJavaType();
                if (rtype instanceof Class) {
                    Class cType = (Class) rtype;
                    if (cType.isInterface()) {
                        if (cType.getName().equals("java.util.List")) {
                            instance = (T) new ArrayList<>();
                        } else if (cType.getName().equals("java.util.Map")) {
                            instance = (T) new LinkedHashMap<>();
                        }
                    }
                }
                if (instance == null) {
                    instance = (T) type.newInstance();
                }
            }
            if (args != null) {
                for (NElement arg : args) {
                    processField(arg, true, instance, element, (Class)to, context);
                }
            }
            List<NElement> body = container.children();
            if (body != null) {
                for (NElement arg : body) {
                    processField(arg, false, instance, element, (Class)to, context);
                }
            }
            T finalInstance = instance;
            InstanceConfigurerContext<T> cc = new InstanceConfigurerContext<T>() {
                @Override
                public T instance() {
                    return finalInstance;
                }

                @Override
                public NElement element() {
                    return element;
                }

                @Override
                public Class<T> to() {
                    return (Class<T>) to;
                }

                @Override
                public <T1> NElement elem(T1 any) {
                    return context.createElement(any);
                }

                @Override
                public <T1> T1 obj(NElement element, Class<T1> clazz) {
                    return context.createObject(element, clazz);
                }
            };
            for (InstanceConfigurer<T> process : postProcess) {
                process.prepareInstance(cc);
            }
            return (T) instance;
        }

        private void processField(NElement arg, boolean isArg, T instance, NElement element, Class<T> to, NElementFactoryContext context) {
            Map<String, TFieldImpl<T>> argFields = isArg ? this.argFields : this.bodyFields;
            if (arg.isSimplePair()) {
                NPairElement pair = arg.asPair().get();
                NElement key = pair.key();
                String expectedName = uniformName(key.asStringValue().get());
                TFieldImpl<T> tField = argFields.get(expectedName);
                if (tField != null) {
                    NElement value = pair.value();
                    if (tField.isCollectionType() && tField.isWrapCollections()) {
                        if (!value.isArray()) {
                            if (tField.isContainerIsCollection()) {
                                if (value.isListContainer()) {
                                    NListContainerElement container = value.asListContainer().get();
                                    NArrayElementBuilder tsonElements = NElement.ofArrayBuilder();
                                    List<NElement> params = container.isParametrized() ? container.asParametrizedContainer().get().params().orNull() : null;
                                    if (params != null) {
                                        tsonElements.addAll(params);
                                    }
                                    if (container.children() != null) {
                                        tsonElements.addAll(container.children());
                                    }
                                    value = tsonElements.build();
                                }
                            } else {
                                value = NElement.ofArray(value);
                            }
                        }
                    }
                    tField.field.write(instance, context.createObject(value, (Class<?>) tField.field.getPropertyType().getJavaType()));
                } else {
                    onBodyNotSupported(instance, arg, isArg, element, to, context);
                }
            } else if (arg.isAnyString()) {
                String expectedName = uniformName(arg.asStringValue().get());
                TFieldImpl<T> tField = argFields.get(expectedName);
                boolean found = false;
                if (tField != null) {
                    if (tField.isUseDefaultWhenMissingValue()) {
                        tField.field.write(instance, tField.getValueWhenMissing());
                    }
                    found = true;
                }
                if (!found) {
                    onBodyNotSupported(instance, arg, isArg, element, to, context);
                }
            } else {
                onBodyNotSupported(instance, arg, isArg, element, to, context);
            }
        }


        private void onBodyNotSupported(T instance, NElement arg, boolean isArg, NElement element, Class<T> to, NElementFactoryContext context) {
            boolean found = false;
            if (!found) {

                List<MissingFieldConfigurer<T>> list = isArg ? onUnsupportedArg : onUnsupportedBody;
                FieldConfigurerContext<T> cc = new FieldConfigurerContext<T>() {
                    @Override
                    public T instance() {
                        return instance;
                    }

                    @Override
                    public NElement field() {
                        return arg;
                    }

                    @Override
                    public NElement element() {
                        return element;
                    }

                    @Override
                    public Class<T> to() {
                        return to;
                    }

                    @Override
                    public <T1> NElement elem(T1 any) {
                        return context.createElement(any);
                    }

                    @Override
                    public <T1> T1 obj(NElement element, Class<T1> clazz) {
                        return context.createObject(element, clazz);
                    }
                };
                for (MissingFieldConfigurer<T> tOnUnsupported : list) {

                    if (tOnUnsupported.prepareField(cc)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                //System.err.println("unsupported " + arg + " in " + to);
            }
        }


        private String uniformName(String s) {
            s = s.trim();
            if (renamer == null) {
                return s;
            }
            return renamer.apply(s);
        }

    }
}
