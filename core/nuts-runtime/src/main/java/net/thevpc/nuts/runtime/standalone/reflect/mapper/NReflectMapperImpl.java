package net.thevpc.nuts.runtime.standalone.reflect.mapper;

import net.thevpc.nuts.reflect.*;
import net.thevpc.nuts.runtime.standalone.reflect.SPath;
import net.thevpc.nuts.runtime.standalone.reflect.TypeConverterKey;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.runtime.standalone.reflect.DefaultConvertersByType;
import net.thevpc.nuts.util.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NReflectMapperImpl implements NReflectMapper {
    public static final TypeMapperRepositoryDef globalMapperRepositoryDef = new TypeMapperRepositoryDef(null);
    private static NReflectConverter defaultConvertersByType = new DefaultConvertersByType();
    private TypeMapperRepositoryDef mapperRepositoryDef;
    private NReflectRepository repository;
    private final TypeMapperTraversedTreeImpl tree;
    private NAssignmentPolicy assignmentPolicy = NAssignmentPolicy.ANY;
    private NEqualizer<Object> eq = NEqualizer.ofDefault();

    private Set<SPath> included = new HashSet<>();
    private Set<SPath> excluded = new HashSet<>();
    private Map<SPath, SPath> renamed = new HashMap<>();
    private Map<TypeConverterKey, NReflectConverter> convertersByType = new HashMap<>();
    private Map<SPath, NReflectConverter> convertersByName = new HashMap<>();

    private SPath root;
    private NReflectMappingStrategy defaultMapper = new NReflectMappingStrategy() {
        @Override
        public boolean copy(Object from, Object to, NReflectMapper context) {
            NReflectType fromType = repository.getType(from.getClass());
            boolean v = false;
            for (NReflectProperty property : fromType.getProperties()) {
                v |= doAction(path(property), property, from, to);
            }
            return v;
        }

        @Override
        public Object mapToType(Object value, NReflectType fromType, NReflectType toType, NReflectMapper context) {
            if (value == null) {
                return null;
            }
            return defaultMapToType(value, toType);
        }
    };

    public NReflectMapperImpl(NReflectRepository reflectRepository) {
        this(reflectRepository, null);
    }

    public NReflectMapperImpl(NReflectRepository reflectRepository, TypeMapperRepositoryDef mapperRepositoryDef) {
        this(reflectRepository, mapperRepositoryDef, null, null);
    }

    public NReflectMapperImpl(NReflectRepository reflectRepository, TypeMapperRepositoryDef mapperRepositoryDef, SPath root) {
        this(reflectRepository, mapperRepositoryDef, root, null);
    }

    public NReflectMapperImpl(NReflectRepository reflectRepository, TypeMapperRepositoryDef mapperRepositoryDef, SPath root, TypeMapperTraversedTreeImpl tree) {
        this.repository = reflectRepository == null ? NReflectRepository.of() : reflectRepository;
        this.mapperRepositoryDef = mapperRepositoryDef == null ? new TypeMapperRepositoryDef(NReflectMapperImpl.globalMapperRepositoryDef) : mapperRepositoryDef;
        this.root = root;
        this.tree = tree == null ? new TypeMapperTraversedTreeImpl() : tree;
    }

    public NReflectMapper setRepository(NReflectRepository repository) {
        if (repository != null) {
            this.repository = repository;
        }
        return this;
    }

    @Override
    public NReflectMapper setPropertyConverter(String property, NReflectConverter converter) {
        if (converter == null) {
            convertersByName.remove(SPath.parse(property));
        } else {
            convertersByName.put(SPath.parse(property), converter);
        }
        return this;
    }

    @Override
    public NReflectMapper setTypeConverter(NReflectType fromType, NReflectType toType, NReflectConverter converter) {
        if (converter == null) {
            convertersByType.remove(new TypeConverterKey(fromType, toType));
        } else {
            convertersByType.put(new TypeConverterKey(fromType, toType), converter);
        }
        return this;
    }

    @Override
    public NReflectMapper includeProperty(String... names) {
        for (String name : names) {
            included.add(SPath.parse(name));
        }
        return this;
    }

    @Override
    public NReflectMapper excludeProperty(String... names) {
        for (String name : names) {
            excluded.add(SPath.parse(name));
        }
        return this;
    }

    @Override
    public NReflectMapper renameProperty(String from, String to) {
        renamed.put(SPath.parse(from), SPath.parse(to));
        return this;
    }

    //    @Override
    public Object get(Object a) {
        return tree.get(a);
    }

    //    @Override
    public Object put(Object a, Object b) {
        return tree.put(a, b);
    }

    @Override
    public NOptional<NReflectMappingStrategy> getMappingStrategy(NReflectType from, NReflectType to) {
        return mapperRepositoryDef.getMappingStrategy(from.asJavaClass().get(), to.asJavaClass().get());
    }

    @Override
    public NEqualizer<Object> getEqualizer() {
        return eq;
    }

    public NReflectMapper setEqualizer(NEqualizer<Object> eq) {
        this.eq = eq == null ? NEqualizer.ofDefault() : eq;
        return this;
    }

    public NAssignmentPolicy getAssignmentPolicy() {
        return assignmentPolicy;
    }

    public NReflectMapperImpl setAssignmentPolicy(NAssignmentPolicy assignmentPolicy) {
        this.assignmentPolicy = assignmentPolicy == null ? NAssignmentPolicy.ANY : assignmentPolicy;
        return this;
    }

    @Override
    public NReflectRepository getRepository() {
        return repository;
    }

    public SPath path(SPath path) {
        if (root == null) {
            return path;
        }
        return root.resolve(path);
    }

    public SPath path(NReflectProperty p) {
        if (root == null) {
            return new SPath(new String[]{p.getName()});
        }
        return root.resolve(p.getName());
    }

    boolean isIncludedPath(SPath path) {
        SPath a = getIncludedPath(path);
        SPath b = getExcludedPath(path);
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

    private boolean doAction(SPath path, NReflectProperty property, Object fromInstance, Object toInstance) {
        if (isIncludedPath(path)) {
            SPath fpath = path(path);
            SPath n = renamed.get(fpath);
            NReflectType toType = repository.getType(toInstance.getClass());
            NOptional<NReflectProperty> toPropOptional = toType.getProperty(n == null ? path.name() : n.name());
            if (toPropOptional.isPresent()) {
                NReflectProperty toProp = toPropOptional.get();
                return assignmentPolicy.applyMappingValue(
                        () -> property.read(fromInstance),
                        () -> toProp.read(toInstance),
                        (sourceValue) -> {
                            NReflectConverter converter = convertersByName.get(fpath);
                            Object newValue = (converter != null)
                                    ? converter.convert(sourceValue, path.toString(), property.getPropertyType(), toProp.getPropertyType(), this)
                                    : mapToType(sourceValue, toProp.getPropertyType());
                            toProp.write(
                                    newValue,
                                    mapToType(newValue, toProp.getPropertyType())
                            );
                            return true;
                        }
                );
            }
        }
        return false;
    }

    @Override
    public Object mapToType(Object value, Type toType) {
        return mapToType(value, repository.getType(toType));
    }

    public Object mapToType(Object value, NReflectType toType) {
        if (toType == null) {
            return value;
        }
        if (value == null) {
            return toType.getDefaultValue();
        }
        NReflectType u = getRepository().getType(value.getClass());
        if (u.equals(toType) || toType.isAssignableFrom(u)) {
            return value;
        }
        NOptional<NReflectMappingStrategy> typeMapper = getMappingStrategy(u, toType);
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
                    (NReflectMappingStrategy) bean
            );
        } else {
            throw new IllegalArgumentException("Invalid TypeMapper type " + bean.getClass());
        }
    }

    SPath getIncludedPath(SPath path) {
        while (path != null) {
            if (included.contains(path)) {
                return path;
            }
            path = path.parent();
        }
        return null;
    }

    SPath getExcludedPath(SPath path) {
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
        NOptional<NReflectMappingStrategy> typeMapper = getMappingStrategy(getRepository().getType(from.getClass()), repository.getType(to.getClass()));
        return typeMapper.get().copy(from, to, this);
    }

    public void tryRegister(Class<?> from, Type to, NReflectMappingStrategy mapper) {
        mapperRepositoryDef.tryRegister(from, to, mapper);
    }

    public void tryRegister(Class<?> from, Class<?> to, NReflectMappingStrategy mapper) {
        mapperRepositoryDef.tryRegister(from, to, mapper);
    }
}
