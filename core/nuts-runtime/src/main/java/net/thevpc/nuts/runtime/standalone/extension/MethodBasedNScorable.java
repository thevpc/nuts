package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScorableContext;

import java.lang.reflect.Method;

class MethodBasedNScorable implements NScorable {
    private final NExtensionTypeInfo nExtensionTypeInfo;
    private final Method declaredMethod;

    public MethodBasedNScorable(NExtensionTypeInfo nExtensionTypeInfo, Method declaredMethod) {
        this.nExtensionTypeInfo = nExtensionTypeInfo;
        this.declaredMethod = declaredMethod;
        declaredMethod.setAccessible(true);
    }

    @Override
    public int getScore(NScorableContext context) {
        try {
            return (int) declaredMethod.invoke(null, context);
        } catch (Exception e) {
            nExtensionTypeInfo.LOG().log(NMsg.ofC("[%s] [%s] error invoking score method %s ", nExtensionTypeInfo.getImplType(), nExtensionTypeInfo.getApiType(), declaredMethod).asSevere());
        }
        return NScorable.UNSUPPORTED_SCORE;
    }
}
