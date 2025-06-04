package net.thevpc.nuts.runtime.standalone.util.reflect.mapper;

import net.thevpc.nuts.reflect.*;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.runtime.standalone.util.reflect.DefaultConvertersByType;
import net.thevpc.nuts.runtime.standalone.util.reflect.NReflectMapperImpl;
import net.thevpc.nuts.util.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NReflectMapperContextImpl implements NReflectMapperContext {
    private static NReflectMapper.Converter defaultConvertersByType = new DefaultConvertersByType();
    private TypeMapperRepositoryDef mapperRepositoryDef;
    private final NReflectMapper mapper;
    private final NReflectRepository repository;
    private final TypeMapperTraversedTreeImpl tree;
    private NMapStrategy mapStrategy = NMapStrategy.ANY;
    private NEqualizer<Object> eq = NEqualizer.ofDefault();

    private Set<NReflectMapperImpl.SPath> included = new HashSet<>();
    private Set<NReflectMapperImpl.SPath> excluded = new HashSet<>();
    private Map<NReflectMapperImpl.SPath, NReflectMapperImpl.SPath> renamed = new HashMap<>();
    private Map<NReflectMapperImpl.TypeConverterKey, NReflectMapper.Converter> convertersByType = new HashMap<>();
    private Map<NReflectMapperImpl.SPath, NReflectMapper.Converter> convertersByName = new HashMap<>();

    private NReflectMapperImpl.SPath root;
    private NReflectTypeMapper defaultMapper = new NReflectTypeMapper() {
        @Override
        public boolean copy(Object from, Object to, NReflectMapperContext context) {
            NReflectType fromType = repository.getType(from.getClass());
            boolean v = false;
            for (NReflectProperty property : fromType.getProperties()) {
                v |= doAction(path(property), property, from, to);
            }
            return v;
        }

        @Override
        public Object mapToType(Object value, NReflectType fromType, NReflectType toType, NReflectMapperContext context) {
            if (value == null) {
                return null;
            }
            return defaultMapToType(value, toType);
        }
    };

    public NReflectMapperContextImpl(NReflectMapper mapper, NReflectRepository reflectRepository, TypeMapperRepositoryDef mapperRepositoryDef) {
        this(mapper, reflectRepository, mapperRepositoryDef, null, null);
    }

    public NReflectMapperContextImpl(NReflectMapper mapper, NReflectRepository reflectRepository, TypeMapperRepositoryDef mapperRepositoryDef, NReflectMapperImpl.SPath root) {
        this(mapper, reflectRepository, mapperRepositoryDef, root, null);
    }

    public NReflectMapperContextImpl(NReflectMapper mapper, NReflectRepository reflectRepository, TypeMapperRepositoryDef mapperRepositoryDef, NReflectMapperImpl.SPath root, TypeMapperTraversedTreeImpl tree) {
        this.mapper = mapper;
        this.repository = reflectRepository == null ? NReflectRepository.of() : reflectRepository;
        this.mapperRepositoryDef = mapperRepositoryDef == null ? new TypeMapperRepositoryDef(NReflectMapperImpl.globalMapperRepositoryDef) : mapperRepositoryDef;
        this.root = root;
        this.tree = tree == null ? new TypeMapperTraversedTreeImpl() : tree;
    }


    @Override
    public void setPropertyConverter(String property, NReflectMapper.Converter converter) {
        if (converter == null) {
            convertersByName.remove(NReflectMapperImpl.SPath.parse(property));
        } else {
            convertersByName.put(NReflectMapperImpl.SPath.parse(property), converter);
        }
    }

    @Override
    public void setTypeConverter(NReflectType fromType, NReflectType toType, NReflectMapper.Converter converter) {
        if (converter == null) {
            convertersByType.remove(new NReflectMapperImpl.TypeConverterKey(fromType, toType));
        } else {
            convertersByType.put(new NReflectMapperImpl.TypeConverterKey(fromType, toType), converter);
        }
    }

    @Override
    public void include(String... names) {
        for (String name : names) {
            included.add(NReflectMapperImpl.SPath.parse(name));
        }
    }

    @Override
    public void excludeProperty(String... names) {
        for (String name : names) {
            excluded.add(NReflectMapperImpl.SPath.parse(name));
        }
    }

    @Override
    public void rename(String from, String to) {
        renamed.put(NReflectMapperImpl.SPath.parse(from), NReflectMapperImpl.SPath.parse(to));
    }

    @Override
    public Object get(Object a) {
        return tree.get(a);
    }

    @Override
    public Object put(Object a, Object b) {
        return tree.put(a, b);
    }

    @Override
    public NOptional<NReflectTypeMapper> findTypeMapper(NReflectType from, NReflectType to) {
        return mapperRepositoryDef.findTypeMapper(from.asJavaClass().get(), to.asJavaClass().get());
    }

    @Override
    public NEqualizer<Object> equalizer() {
        return eq;
    }

    public NReflectMapperContext setEqualizer(NEqualizer<Object> eq) {
        this.eq = eq == null ? NEqualizer.ofDefault() : eq;
        return this;
    }

    public NMapStrategy mapStrategy() {
        return mapStrategy;
    }

    public NReflectMapperContextImpl setMapStrategy(NMapStrategy mapStrategy) {
        this.mapStrategy = mapStrategy==null?NMapStrategy.ANY : mapStrategy;
        return this;
    }

    @Override
    public NReflectRepository repository() {
        return repository;
    }

    @Override
    public NReflectMapper mapper() {
        return mapper;
    }

    public NReflectMapperImpl.SPath path(NReflectMapperImpl.SPath path) {
        if (root == null) {
            return path;
        }
        return root.resolve(path);
    }

    public NReflectMapperImpl.SPath path(NReflectProperty p) {
        if (root == null) {
            return new NReflectMapperImpl.SPath(new String[]{p.getName()});
        }
        return root.resolve(p.getName());
    }

    boolean isIncludedPath(NReflectMapperImpl.SPath path) {
        NReflectMapperImpl.SPath a = getIncludedPath(path);
        NReflectMapperImpl.SPath b = getExcludedPath(path);
        if (a == null && b == null) {
            return true;
        } else if (a == null) {
            return false;
        } else if (b == null) {
            return true;
        } else if (a.elems.length == b.elems.length) {
            return false;
        } else if (a.elems.length > b.elems.length) {
            return true;
        } else {
            return false;
        }
    }

    private boolean doAction(NReflectMapperImpl.SPath path, NReflectProperty property, Object fromInstance, Object toInstance) {
        if (isIncludedPath(path)) {
            NReflectMapperImpl.SPath fpath = path(path);
            NReflectMapperImpl.SPath n = renamed.get(fpath);
            NReflectType toType = repository.getType(toInstance.getClass());
            NOptional<NReflectProperty> toProp = toType.getProperty(n == null ? path.name() : n.name());
            if (toProp.isPresent()) {
                Object sourceValue = property.read(fromInstance);
                if (acceptValue(sourceValue, mapStrategy.source())) {
                    switch (mapStrategy.target()) {
                        case ANY: {
                            NReflectMapper.Converter c = convertersByName.get(fpath);
                            if (c == null) {
                                toProp.get().write(
                                        toInstance,
                                        mapToType(sourceValue, toProp.get().getPropertyType())
                                );
                            } else {
                                toProp.get().write(
                                        toInstance,
                                        c.convert(sourceValue, path.toString(), property.getPropertyType(), toProp.get().getPropertyType(), this)
                                );
                            }
                            return true;
                        }
                        default: {
                            if (acceptValue(toProp.get().read(toInstance), mapStrategy.target())) {
                                Object toValue;
                                NReflectMapper.Converter c = convertersByName.get(fpath);
                                if (c == null) {
                                    toValue = mapToType(sourceValue, toProp.get().getPropertyType());
                                } else {
                                    toValue = c.convert(sourceValue, path.toString(), property.getPropertyType(), toProp.get().getPropertyType(), this);
                                }
                                if (acceptValue(toValue, mapStrategy.target())) {
                                    toProp.get().write(toInstance, toValue);
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean acceptValue(Object value, NMapSideStrategy filter) {
        switch (filter) {
            case BLANK: {
                return NBlankable.isBlank(value);
            }
            case NON_BLANK: {
                return !NBlankable.isBlank(value);
            }
            case NULL: {
                return (value == null);
            }
            case NON_NULL: {
                return (value != null);
            }
        }
        return true;
    }

    public Object mapToType(Object value, NReflectType toType) {
        if (toType == null) {
            return value;
        }
        if (value == null) {
            return toType.getDefaultValue();
        }
        NReflectType u = repository().getType(value.getClass());
        if (u.equals(toType) || toType.isAssignableFrom(u)) {
            return value;
        }
        NOptional<NReflectTypeMapper> typeMapper = findTypeMapper(u, toType);
        if (!typeMapper.isPresent()) {
            return defaultMapper.mapToType(value, u, toType, this);
        }
        return typeMapper.get().mapToType(value, u, toType, this);
    }

    public void tryRegisterBean(Object bean) {
        Class baseClass = JavaClassUtils.unwrapCGLib(bean.getClass());
        Type genericSuperclass = baseClass.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
            tryRegister(
                    (Class) actualTypeArguments[0],
                    (Class) actualTypeArguments[1],
                    (NReflectTypeMapper) bean
            );
        } else {
            throw new IllegalArgumentException("Invalid TypeMapper type " + bean.getClass());
        }
    }

    NReflectMapperImpl.SPath getIncludedPath(NReflectMapperImpl.SPath path) {
        while (path != null) {
            if (included.contains(path)) {
                return path;
            }
            path = path.parent();
        }
        return null;
    }

    NReflectMapperImpl.SPath getExcludedPath(NReflectMapperImpl.SPath path) {
        while (path != null) {
            if (excluded.contains(path)) {
                return path;
            }
            path = path.parent();
        }
        return null;
    }

    public Object defaultMapToType(Object value, NReflectType toType) {
        if (value == null) {
            return null;
        }
        Object ni = toType.newInstance();
        copy(value, ni);
        return ni;
    }


    public boolean copy(Object from, Object to) {
        if (from == null) {
            return false;
        }
        if (to == null) {
            return false;
        }
        NOptional<NReflectTypeMapper> typeMapper = findTypeMapper(repository().getType(from.getClass()), repository.getType(to.getClass()));
        return typeMapper.get().copy(from, to, this);
    }

    public void tryRegister(Class<?> from, Type to, NReflectTypeMapper mapper) {
        mapperRepositoryDef.tryRegister(from, to, mapper);
    }

    public void tryRegister(Class<?> from, Class<?> to, NReflectTypeMapper mapper) {
        mapperRepositoryDef.tryRegister(from, to, mapper);
    }
}
