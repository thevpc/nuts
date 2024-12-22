package net.thevpc.nuts.runtime.standalone.util.reflect;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.reflect.NReflectCopy;
import net.thevpc.nuts.reflect.NReflectProperty;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class NReflectCopyImpl implements NReflectCopy {
    public static final Converter IDENTITY_CONVERTER = new Converter() {
        @Override
        public Object convert(Object value, NReflectType fromType, NReflectType toType, Context context) {
            return value;
        }
    };
    private static Converter defaultConvertersByType = new DefaultConvertersByType();
    private NReflectRepository reflectRepository;
    private Set<SPath> included = new HashSet<>();
    private Set<SPath> excluded = new HashSet<>();
    private Map<SPath, SPath> renamed = new HashMap<>();
    private Map<TypeConverterKey, Converter> convertersByType = new HashMap<>();
    private Map<SPath, Converter> convertersByName = new HashMap<>();

    private static class TypeConverterKey {
        private NReflectType fromType;
        private NReflectType toType;

        public TypeConverterKey(NReflectType fromType, NReflectType toType) {
            this.fromType = fromType;
            this.toType = toType;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            TypeConverterKey that = (TypeConverterKey) o;
            return Objects.equals(fromType, that.fromType) && Objects.equals(toType, that.toType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fromType, toType);
        }
    }

    private static class SPath {
        private String[] elems;

        public static SPath parse(String elem) {
            return new SPath(NStringUtils.split(elem, ".", true, true).toArray(new String[0]));
        }

        public SPath(String[] elems) {
            this.elems = elems;
            if (elems.length < 1) {
                throw new IllegalArgumentException("empty array");
            }
        }

        public SPath parent() {
            if (elems.length == 1) {
                return null;
            }
            return new SPath(Arrays.copyOfRange(elems, 0, elems.length - 1));
        }

        public SPath resolve(String other) {
            String[] a = new String[elems.length + 1];
            System.arraycopy(a, 0, a, 0, elems.length);
            a[elems.length] = other;
            return new SPath(a);
        }

        @Override
        public String toString() {
            return String.join(".", elems);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            SPath sPath = (SPath) o;
            return Objects.deepEquals(elems, sPath.elems);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(elems);
        }

        public String name() {
            return elems[elems.length - 1];
        }

        public SPath resolve(SPath path) {
            List<String> all = new ArrayList<>(elems.length + path.elems.length);
            all.addAll(Arrays.asList(elems));
            all.addAll(Arrays.asList(path.elems));
            return new SPath(all.toArray(new String[0]));
        }

        public SPath skipFirst() {
            if (elems.length == 1) {
                return null;
            }
            return new SPath(Arrays.copyOfRange(elems, 1, elems.length));
        }
    }

    @Override
    public void setPropertyConverter(String property, Converter converter) {
        if (converter == null) {
            convertersByName.remove(SPath.parse(property));
        } else {
            convertersByName.put(SPath.parse(property), converter);
        }
    }

    @Override
    public void setTypeConverter(NReflectType fromType, NReflectType toType, Converter converter) {
        if (converter == null) {
            convertersByType.remove(new TypeConverterKey(fromType, toType));
        } else {
            convertersByType.put(new TypeConverterKey(fromType, toType), converter);
        }
    }

    @Override
    public NReflectRepository getReflectRepository() {
        return reflectRepository;
    }

    @Override
    public NReflectCopy setReflectRepository(NReflectRepository reflectRepository) {
        this.reflectRepository = reflectRepository;
        return this;
    }

    @Override
    public Object convert(Object from, NReflectType to) {
        if (from == null) {
            return to.getDefaultValue();
        }
        DefaultContext ctx = new DefaultContext();
        ctx.included = included;
        ctx.excluded = excluded;
        ctx.reflectRepository = reflectRepository == null ? NReflectRepository.of() : reflectRepository;
        return ctx.convertObject(from, ctx.reflectRepository.getType(from.getClass()), to);
    }

    @Override
    public void copy(Object from, Object to) {
        if (from == null || to == null) {
            return;
        }
        DefaultContext ctx = new DefaultContext();
        ctx.included = included;
        ctx.excluded = excluded;
        ctx.reflectRepository = reflectRepository == null ? NReflectRepository.of() : reflectRepository;
        ctx.copyAll(from, to);
    }

    @Override
    public void include(String... names) {
        for (String name : names) {
            included.add(SPath.parse(name));
        }
    }

    @Override
    public void excludeProperty(String... names) {
        for (String name : names) {
            excluded.add(SPath.parse(name));
        }
    }

    @Override
    public void rename(String from, String to) {
        renamed.put(SPath.parse(from), SPath.parse(to));
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    private class DefaultContext implements Context {
        private SPath root;
        private NReflectRepository reflectRepository;
        private Set<SPath> included = new HashSet<>();
        private Set<SPath> excluded = new HashSet<>();

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

        public NReflectRepository getReflectRepository() {
            return reflectRepository;
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
                NReflectType toType = reflectRepository.getType(toInstance.getClass());
                NOptional<NReflectProperty> toProp = toType.getProperty(n == null ? path.name() : n.name());
                if (toProp.isPresent()) {
                    Converter c = convertersByName.get(fpath);
                    if (c == null) {
                        toProp.get().write(
                                toInstance,
                                convertObject(property.read(fromInstance), property.getPropertyType(), toProp.get().getPropertyType())
                        );
                    } else {
                        toProp.get().write(
                                toInstance,
                                c.convert(property.read(fromInstance), property.getPropertyType(), toProp.get().getPropertyType(), this)
                        );
                    }
                    return true;

                }
            }
            return false;
        }

        public Object convertObject(Object value, NReflectType fromType, NReflectType toType) {
            if (value == null) {
                return null;
            }
            Converter c = convertersByType.get(new TypeConverterKey(fromType, toType));
            if (c != null) {
                return c.convert(value, fromType, toType, this);
            }
            return defaultConvertObject(value, fromType, toType);
        }

        public Object defaultConvertObject(Object value, NReflectType fromType, NReflectType toType) {
            if (value == null) {
                return null;
            }
            Object converted = defaultConvertersByType.convert(value, fromType, toType, this);
            if (converted != null) {
                return converted;
            }
            Object ni = toType.newInstance();
            DefaultContext ctx = new DefaultContext();
            ctx.included = included.stream().map(x -> x.skipFirst()).filter(x -> x != null).collect(Collectors.toSet());
            ctx.excluded = excluded.stream().map(x -> x.skipFirst()).filter(x -> x != null).collect(Collectors.toSet());
            ctx.reflectRepository = reflectRepository;
            ctx.copyAll(value, ni);
            return ni;
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

        public void copyAll(Object from, Object to) {
            NReflectType fromType = this.reflectRepository.getType(from.getClass());
            for (NReflectProperty property : fromType.getProperties()) {
                this.doAction(this.path(property), property, from, to);
            }
        }
    }
}
