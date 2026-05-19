package net.thevpc.nuts.runtime.standalone.elem.mapper.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.reflect.NReflectProperty;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NReflectUtils;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

class NElementMapperFromBuilder<T> implements NElementDeserializer<T> {
    private DefaultNElementDeserializerBuilder<T> builder;
    NElementDeserializerInstanceFactory<T> onNewInstance;
    NReflectType type;
    List<NElementDeserializerInitializer<T>> postProcess = new ArrayList<>();


    Function<String, String> fieldNameNormalizer;
    Predicate<String> paramFieldFieldFilter;
    Predicate<String> bodyFieldNameFilter;
    //        List<TFieldImpl<T>> allTFields = new ArrayList<>();
    //        boolean built = false;
//        boolean wrapCollections = true;
//        boolean containerIsCollection = false;
    List<NElementDeserializerFieldConfigurer<T>> onUnsupportedBody = new ArrayList<>();
    List<NElementDeserializerFieldConfigurer<T>> onUnsupportedArg = new ArrayList<>();
//        Map<Type, Object> defaultValueByType = new HashMap<>();

    public NElementMapperFromBuilder(DefaultNElementDeserializerBuilder<T> builder) {
        this.onNewInstance = builder.onNewInstance;
        this.builder = builder;
        this.type = builder.type;
        this.paramFieldFieldFilter = builder.paramFieldFieldFilter;
        this.bodyFieldNameFilter = builder.bodyFieldNameFilter;
        this.postProcess.addAll(builder.postProcess);
        this.onUnsupportedBody.addAll(builder.onUnsupportedBody);
        this.onUnsupportedArg.addAll(builder.onUnsupportedArg);
        this.fieldNameNormalizer = builder.fieldNameNormalizer;
    }

    @Override
    public T toObject(NElementDeserializerContext context) {
        NElement element = context.element();
        Type instanceType = context.instanceType();
        NListContainerElement container = element.toListContainer().get();
        List<NElement> args = container.isParametrized() ? container.asParametrizedContainer().get().params().orNull() : null;
        T instance = null;
        if (onNewInstance != null) {
            instance = onNewInstance.newInstance(context);
        }
        if (instance == null) {
            Type rtype = type.javaType();
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
        NReflectType effectiveType=type.repository().getType(instance.getClass());
        //now that we have the instance lets compute
        Map<String, NElementDeserializerBuilderNElementDeserializerFieldImpl<T>> allFields = new HashMap<>();
        Map<String, NElementDeserializerBuilderNElementDeserializerFieldImpl<T>> argFields = new HashMap<>();
        Map<String, NElementDeserializerBuilderNElementDeserializerFieldImpl<T>> bodyFields = new HashMap<>();

        for (NReflectProperty property : effectiveType.properties()) {
            if (!allFields.containsKey(property.getName())) {
                NElementDeserializerBuilderNElementDeserializerFieldImpl<T> o = (NElementDeserializerBuilderNElementDeserializerFieldImpl<T>) builder.preConfiguredFields.get(property.getName());
                if(o!=null){
                    o=o.copy();
                }else{
                    o=new NElementDeserializerBuilderNElementDeserializerFieldImpl<>(property.getName(),builder);
                }
                NElementDeserializerBuilderNElementDeserializerFieldImpl<T> f = o;
                if(o.isIgnored()){
                    continue;
                }
                f.uniformName = uniformName(f.name);
                f.field = null;
                for (NReflectProperty field : effectiveType.properties()) {
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
                    if(f.aliases!=null){
                        for (String alias : f.aliases) {
                            bodyFields.put(uniformName(alias), f);
                        }
                    }
                }
                if (arg) {
                    argFields.put(f.uniformName, f);
                    if(f.aliases!=null){
                        for (String alias : f.aliases) {
                            argFields.put(uniformName(alias), f);
                        }
                    }
                }
                allFields.put(property.getName(), f);
                if(f.aliases!=null){
                    for (String alias : f.aliases) {
                        allFields.put(uniformName(alias), f);
                    }
                }
            }
        }

        if (args != null) {
            for (NElement arg : args) {
                processField(arg, true, instance, element, instanceType, context, argFields, bodyFields);
            }
        }
        List<NElement> body = container.children();
        if (body != null) {
            for (NElement arg : body) {
                processField(arg, false, instance, element, instanceType, context, argFields, bodyFields);
            }
        }
        T finalInstance = instance;
        NElementDeserializerInstanceContext<T> cc = new NElementDeserializerInstanceContextImpl<T>(finalInstance, element, instanceType, context);
        for (NElementDeserializerInitializer<T> process : postProcess) {
            process.initializeInstance(cc);
        }
        return (T) instance;
    }

    private void processField(NElement arg, boolean isArg, T instance, NElement element, Type instanceType, NElementFactoryContext context,
                              Map<String, NElementDeserializerBuilderNElementDeserializerFieldImpl<T>> argFields,
                              Map<String, NElementDeserializerBuilderNElementDeserializerFieldImpl<T>> bodyFields
    ) {
        Map<String, NElementDeserializerBuilderNElementDeserializerFieldImpl<T>> argFields2 = isArg ? argFields : bodyFields;
        if (arg.isSimplePair()) {
            NPairElement pair = arg.asPair().get();
            NElement key = pair.key();
            String expectedName = uniformName(key.asStringValue().get());
            NElementDeserializerBuilderNElementDeserializerFieldImpl<T> tField = argFields2.get(expectedName);
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
                Class<?> jt;
                if(tField.typeOverride!=null) {
                    jt = (Class<?>) NReflectUtils.getRawClass(tField.typeOverride).orNull();
                    if(jt==null){
                        jt = (Class<?>) tField.field.getPropertyType().javaType();
                    }
                }else{
                    jt = (Class<?>) tField.field.getPropertyType().javaType();
                }
                if((jt.isArray() || Collection.class.isAssignableFrom(jt)) && !value.isAnyArray()) {
                    tField.field.write(instance, context.toObject(value.wrapIntoArray(), jt));
                }else {
                    tField.field.write(instance, context.toObject(value, jt));
                }
            } else {
                onBodyNotSupported(instance, arg, isArg, element, instanceType, context);
            }
        } else if (arg.isAnyString()) {
            String expectedName = uniformName(arg.asStringValue().get());
            NElementDeserializerBuilderNElementDeserializerFieldImpl<T> tField = argFields2.get(expectedName);
            boolean found = false;
            if (tField != null) {
                if (tField.isUseDefaultWhenMissingValue()) {
                    tField.field.write(instance, tField.getValueWhenMissing());
                }
                found = true;
            }
            if (!found) {
                onBodyNotSupported(instance, arg, isArg, element, instanceType, context);
            }
        } else {
            onBodyNotSupported(instance, arg, isArg, element, instanceType, context);
        }
    }


    private void onBodyNotSupported(T instance, NElement arg, boolean isArg, NElement element, Type instanceType, NElementFactoryContext context) {
        boolean found = false;
        if (!found) {
            List<NElementDeserializerFieldConfigurer<T>> list = isArg ? onUnsupportedArg : onUnsupportedBody;
            NElementDeserializerFieldContext<T> cc = new NElementDeserializerFieldContextImpl<T>(instance, arg, element, instanceType, context);
            for (NElementDeserializerFieldConfigurer<T> tOnUnsupported : list) {
                if (tOnUnsupported.configureField(cc)) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {

        }
    }

    private String uniformName(String s) {
        s = s.trim();
        if(fieldNameNormalizer !=null){
            return fieldNameNormalizer.apply(s);
        }
        return s;
    }

}
