package net.thevpc.nuts.runtime.standalone.util.reflect;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.reflect.*;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.runtime.standalone.util.reflect.mapper.*;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class NReflectMapperImpl implements NReflectMapper {
    public static final Converter IDENTITY_CONVERTER = new Converter() {
        @Override
        public Object convert(Object value, String path, NReflectType fromType, NReflectType toType, NReflectMapperContext context) {
            return value;
        }
    };
    private NReflectRepository reflectRepository;
    public static final TypeMapperRepositoryDef globalMapperRepositoryDef = new TypeMapperRepositoryDef(null);

    public static class TypeConverterKey {
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

    public static class SPath {
        public String[] elems;

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
    public NReflectRepository getReflectRepository() {
        return reflectRepository;
    }

    @Override
    public NReflectMapper setReflectRepository(NReflectRepository reflectRepository) {
        this.reflectRepository = reflectRepository;
        return this;
    }


    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }





    public <A, B> B mapToType(A a, Class<B> b) {
        return (B) createContext().mapToType(a, reflectRepository.getType(b));
    }


    public NReflectMapperContext createContext() {
        return new NReflectMapperContextImpl(this,reflectRepository,new TypeMapperRepositoryDef(globalMapperRepositoryDef));
    }



    public Object mapToType(Object from, Type to) {
        return mapToType(from, reflectRepository.getType(to));
    }

    @Override
    public Object mapToType(Object from, NReflectType to) {
        if (from == null) {
            return to.getDefaultValue();
        }
        NReflectMapperContext ctx = createContext();

        return ctx.mapToType(from, to);
    }

    @Override
    public boolean copy(Object from, Object to) {
        if (from == null || to == null) {
            return false;
        }
        NReflectMapperContext ctx = createContext();
        return ctx.copy(from, to);
    }


}
