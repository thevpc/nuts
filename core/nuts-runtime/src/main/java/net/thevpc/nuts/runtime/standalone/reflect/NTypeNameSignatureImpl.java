package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.reflect.NSignatureDomain;
import net.thevpc.nuts.reflect.NTypeName;
import net.thevpc.nuts.reflect.NTypeNameDomain;
import net.thevpc.nuts.reflect.NTypeNameSignature;

public class NTypeNameSignatureImpl extends NSignatureBase<NTypeName<?>, NTypeNameSignature> implements NTypeNameSignature {

    public static NTypeNameSignature of(NTypeNameDomain domain, NTypeName... types) {
        return new NTypeNameSignatureImpl(null, types, false, domain);
    }

    public static NTypeNameSignature ofVarArgs(NTypeNameDomain domain, NTypeName... types) {
        NSignatureBase.checkVararg(types, domain);
        return new NTypeNameSignatureImpl(null, types, true, domain);
    }

    public static NTypeNameSignature of(NTypeNameDomain domain, String name, NTypeName... types) {
        return new NTypeNameSignatureImpl(name, types, false, domain);
    }

    public static NTypeNameSignature ofVarArgs(NTypeNameDomain domain, String name, NTypeName[] types) {
        NSignatureBase.checkVararg(types, domain);
        return new NTypeNameSignatureImpl(name, types, true, domain);
    }

    private NTypeNameSignatureImpl(String name, NTypeName[] types, boolean vararg, NTypeNameDomain domain) {
        super(name, types, vararg, (NSignatureDomain) domain);
    }

    @Override
    protected NTypeNameSignature _create(String name, NTypeName[] types, boolean vararg) {
        NTypeNameDomain z = (NTypeNameDomain) domain();
        return new NTypeNameSignatureImpl(name, types, vararg, z);
    }


}
