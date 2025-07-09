package net.thevpc.nuts.runtime.standalone.format.elem.mapper.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.reflect.NReflectProperty;
import net.thevpc.nuts.reflect.NReflectType;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

class NElementMapperFromBuilder<T> implements NElementMapper<T> {
    private DefaultNElementMapperBuilder<T> builder;
    NElementMapperBuilderInstanceFactory<T> onNewInstance;
    NReflectType type;
    List<NElementMapperBuilderInitializer<T>> postProcess = new ArrayList<>();


    Function<String, String> renamer;
    Predicate<String> paramFieldFieldFilter;
    Predicate<String> bodyFieldNameFilter;
    //        List<TFieldImpl<T>> allTFields = new ArrayList<>();
    //        boolean built = false;
//        boolean wrapCollections = true;
//        boolean containerIsCollection = false;
    List<NElementMapperBuilderFieldConfigurer<T>> onUnsupportedBody = new ArrayList<>();
    List<NElementMapperBuilderFieldConfigurer<T>> onUnsupportedArg = new ArrayList<>();
//        Map<Type, Object> defaultValueByType = new HashMap<>();

    public NElementMapperFromBuilder(DefaultNElementMapperBuilder<T> builder) {
        this.onNewInstance = builder.onNewInstance;
        this.builder = builder;
        this.type = builder.type;
        this.paramFieldFieldFilter = builder.paramFieldFieldFilter;
        this.bodyFieldNameFilter = builder.bodyFieldNameFilter;
        this.postProcess.addAll(builder.postProcess);
        this.onUnsupportedBody.addAll(builder.onUnsupportedBody);
        this.onUnsupportedArg.addAll(builder.onUnsupportedArg);
        this.renamer = builder.renamer;
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
            instance = onNewInstance.newInstance(new NElementMapperBuilderFactoryContextImpl<>(element, to, context));
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
        NReflectType effectiveType=type.getRepository().getType(instance.getClass());
        //now that we have the instance lets compute
        Map<String, NElementMapperBuilderFieldImpl<T>> allFields = new HashMap<>();
        Map<String, NElementMapperBuilderFieldImpl<T>> argFields = new HashMap<>();
        Map<String, NElementMapperBuilderFieldImpl<T>> bodyFields = new HashMap<>();

        for (NReflectProperty property : effectiveType.getProperties()) {
            if (!allFields.containsKey(property.getName())) {
                NElementMapperBuilderFieldImpl<T> f = new NElementMapperBuilderFieldImpl<>(property.getName(), builder);
                f.uniformName = uniformName(f.name);
                f.field = null;
                for (NReflectProperty field : effectiveType.getProperties()) {
                    String u = uniformName(field.getName());
                    if (u.equals(f.uniformName)) {
                        f.field = field;
                        break;
                    }
                }
                f.wrapCollections=builder.wrapCollections;
                f.containerIsCollection=builder.containerIsCollection;
                f.arg= paramFieldFieldFilter == null || paramFieldFieldFilter.test(property.getName());
                f.body= bodyFieldNameFilter == null || bodyFieldNameFilter.test(property.getName());
                boolean body = f.body || (!f.arg && !f.body);
                boolean arg = f.arg || (!f.arg && !f.body);
                if (body) {
                    bodyFields.put(f.uniformName, f);
                }
                if (arg) {
                    argFields.put(f.uniformName, f);
                }
                allFields.put(property.getName(), f);
            }
        }

        if (args != null) {
            for (NElement arg : args) {
                processField(arg, true, instance, element, (Class) to, context, argFields, bodyFields);
            }
        }
        List<NElement> body = container.children();
        if (body != null) {
            for (NElement arg : body) {
                processField(arg, false, instance, element, (Class) to, context, argFields, bodyFields);
            }
        }
        T finalInstance = instance;
        NElementMapperBuilderInstanceContext<T> cc = new NElementMapperBuilderInstanceContextImpl<T>(finalInstance, element, to, context);
        for (NElementMapperBuilderInitializer<T> process : postProcess) {
            process.initializeInstance(cc);
        }
        return (T) instance;
    }

    private void processField(NElement arg, boolean isArg, T instance, NElement element, Class<T> to, NElementFactoryContext context,
                              Map<String, NElementMapperBuilderFieldImpl<T>> argFields,
                              Map<String, NElementMapperBuilderFieldImpl<T>> bodyFields
    ) {
        Map<String, NElementMapperBuilderFieldImpl<T>> argFields2 = isArg ? argFields : bodyFields;
        if (arg.isSimplePair()) {
            NPairElement pair = arg.asPair().get();
            NElement key = pair.key();
            String expectedName = uniformName(key.asStringValue().get());
            NElementMapperBuilderFieldImpl<T> tField = argFields2.get(expectedName);
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
                Class<?> jt = (Class<?>) tField.field.getPropertyType().getJavaType();
                if((jt.isArray() || Collection.class.isAssignableFrom(jt)) && !value.isAnyArray()) {
                    tField.field.write(instance, context.createObject(value.wrapIntoArray(), jt));
                }else {
                    tField.field.write(instance, context.createObject(value, jt));
                }
            } else {
                onBodyNotSupported(instance, arg, isArg, element, to, context);
            }
        } else if (arg.isAnyString()) {
            String expectedName = uniformName(arg.asStringValue().get());
            NElementMapperBuilderFieldImpl<T> tField = argFields2.get(expectedName);
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
            List<NElementMapperBuilderFieldConfigurer<T>> list = isArg ? onUnsupportedArg : onUnsupportedBody;
            NElementMapperBuilderFieldContext<T> cc = new NElementMapperBuilderFieldContextImpl<T>(instance, arg, element, to, context);
            for (NElementMapperBuilderFieldConfigurer<T> tOnUnsupported : list) {

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
