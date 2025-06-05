package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
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
    public Predicate<Type> getIndestructibleTypesFilter() {
        return base.getIndestructibleTypesFilter();
    }

    @Override
    public boolean isIndestructibleObject(Object any) {
        return base.isIndestructibleObject(any);
    }

    @Override
    public boolean isIndestructibleType(Type any) {
        return base.isIndestructibleType(any);
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
    public Map<String, Object> getProperties() {
        return base.getProperties();
    }

    @Override
    public NElement defaultCreateElement(Object o, Type expectedType) {
        return base.defaultCreateElement(o, expectedType);
    }

    @Override
    public Object defaultDestruct(Object o, Type expectedType) {
        return base.defaultDestruct(o, expectedType);
    }

    @Override
    public NElement createElement(Object o) {
        return base.createElement(o);
    }

    @Override
    public NElement createElement(Object o, Type expectedType) {
        return base.createElement(o, expectedType);
    }

    @Override
    public Object destruct(Object o, Type expectedType) {
        return base.destruct(o, expectedType);
    }

    @Override
    public <T> T createObject(NElement o, Class<T> type) {
        return base.createObject(o, type);
    }

    @Override
    public Object createObject(NElement o, Type type) {
        return base.createObject(o, type);
    }

    @Override
    public <T> T defaultCreateObject(NElement o, Class<T> type) {
        return base.defaultCreateObject(o, type);
    }

    @Override
    public <T> T defaultCreateObject(NElement o, Type type) {
        return base.defaultCreateObject(o, type);
    }

    @Override
    public boolean isNtf() {
        return base.isNtf();
    }

    @Override
    public NReflectRepository getTypesRepository() {
        return base.getTypesRepository();
    }

    @Override
    public <T> NElementMapper<T> getMapper(Type expectedType, boolean defaultOnly) {
        return base.getMapper(expectedType, defaultOnly);
    }

    @Override
    public <T> NElementMapper<T> getMapper(NElement element, boolean defaultOnly) {
        return base.getMapper(element, defaultOnly);
    }
}
