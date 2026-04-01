package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.reflect.NReflectSignature;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NSignatureDomain;

public class NReflectSignatureImpl extends NSignatureBase<NReflectType, NReflectSignature> implements NReflectSignature {

    public static final NSignatureDomain<NReflectType> DOMAIN = new NSignatureDomain<NReflectType>() {
        @Override
        public boolean isArray(NReflectType type) {
            return type.isArrayType();
        }

        @Override
        public NReflectType getComponentType(NReflectType type) {
            return type.getComponentType();
        }

        @Override
        public String toSignatureString(NReflectType type) {
            return type.getName();
        }

        @Override
        public boolean isPrimitive(NReflectType a) {
            return a.isPrimitive();
        }

        public NReflectType toBoxedType(NReflectType a) {
            return a.getBoxedType().orElse(a);
        }

        @Override
        public NReflectType toPrimitiveType(NReflectType a) {
            return a.getPrimitiveType().orNull();
        }


        @Override
        public boolean isAssignableFrom(NReflectType a, NReflectType b) {
            return a.isAssignableFrom(b);
        }

        @Override
        public boolean isInterface(NReflectType type) {
            return type.isInterface();
        }

        @Override
        public NReflectType[] getInterfaces(NReflectType type) {
            return type.getInterfaces();
        }

        @Override
        public NReflectType getSuperType(NReflectType type) {
            return type.getSuperType();
        }
    };

    public static NReflectSignature of(NReflectType... types) {
        return new NReflectSignatureImpl(null, types, false);
    }

    public static NReflectSignature ofVarArgs(NReflectType... types) {
        NSignatureBase.checkVararg(types, DOMAIN);
        return new NReflectSignatureImpl(null, types, true);
    }

    public static NReflectSignature of(String name, NReflectType... types) {
        return new NReflectSignatureImpl(name, types, false);
    }

    public static NReflectSignature ofVarArgs(String name, NReflectType... types) {
        NSignatureBase.checkVararg(types, DOMAIN);
        return new NReflectSignatureImpl(name, types, true);
    }

    private NReflectSignatureImpl(String name, NReflectType[] types, boolean vararg) {
        super(name, types, vararg, DOMAIN);
    }

    @Override
    protected NReflectSignature _create(String name, NReflectType[] types, boolean vararg) {
        return new NReflectSignatureImpl(name, types, vararg);
    }
}
