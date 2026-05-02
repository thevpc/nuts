package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.reflect.NReflectRepository;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Predicate;

public class NElementFactoryContextAdapter implements NElementFactoryContext {
    private NElementFactoryContext base;

    public NElementFactoryContextAdapter(NElementFactoryContext base) {
        this.base = base;
    }

    @Override
    public Predicate<Type> getSimpleTypesFilter() {
        return base.getSimpleTypesFilter();
    }

    @Override
    public boolean isSimpleObject(Object any) {
        return base.isSimpleObject(any);
    }

    @Override
    public boolean isSimpleType(Type any) {
        return base.isSimpleType(any);
    }

    @Override
    public boolean isAtomicObject(Object any) {
        return base.isAtomicObject(any);
    }

    @Override
    public boolean isAtomicType(Type any) {
        return base.isAtomicType(any);
    }

    @Override
    public Map<String, Object> getProperties() {
        return base.getProperties();
    }

    @Override
    public NElement defaultCreateElement(Object o, Type expectedType) {
        return base.defaultCreateElement(o, expectedType);
    }

    @Override
    public Object defaultToSimple(Object o, Type expectedType) {
        return base.defaultToSimple(o, expectedType);
    }

    @Override
    public NElement toElement(Object o) {
        return base.toElement(o);
    }

    @Override
    public NElement toElement(Object o, Type expectedType) {
        return base.toElement(o, expectedType);
    }

    @Override
    public Object toSimple(Object o, Type expectedType) {
        return base.toSimple(o, expectedType);
    }

    @Override
    public <T> T toObject(NElement o, Class<T> type) {
        return base.toObject(o, type);
    }

    @Override
    public Object toObject(NElement o, Type type) {
        return base.toObject(o, type);
    }

    @Override
    public <T> T defaultToObject(NElement o, Class<T> type) {
        return base.defaultToObject(o, type);
    }

    @Override
    public <T> T defaultToObject(NElement o, Type type) {
        return base.defaultToObject(o, type);
    }

    @Override
    public boolean isNtf() {
        return base.isNtf();
    }

    @Override
    public NReflectRepository getTypesRepository() {
        return base.getTypesRepository();
    }

//    @Override
//    public <T> NElementMapper<T> getMapper(Type expectedType, boolean defaultOnly) {
//        return base.getMapper(expectedType, defaultOnly);
//    }
//
//    @Override
//    public <T> NElementMapper<T> getMapper(NElement element, boolean defaultOnly) {
//        return base.getMapper(element, defaultOnly);
//    }

    @Override
    public <T> NElementSerializer<T> getSerializer(Type type, boolean defaultOnly) {
        return base.getSerializer(type, defaultOnly);
    }

    @Override
    public <T> NElementSimplifier<T> getSimplifier(Type type, boolean defaultOnly) {
        return base.getSimplifier(type, defaultOnly);
    }

    @Override
    public <T> NElementDeserializer<T> getDeserializer(Type type, boolean defaultOnly) {
        return base.getDeserializer(type, defaultOnly);
    }

    @Override
    public <T> NElementDeserializer<T> getDeserializer(NElement element, boolean defaultOnly) {
        return base.getDeserializer(element, defaultOnly);
    }
}
