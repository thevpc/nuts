package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;

public class NBeanConstructorContextImpl implements NBeanConstructorContext {
    public static final NBeanConstructorContext INSTANCE = new NBeanConstructorContextImpl();

    @Override
    public boolean isSupported(Class<?> paramType) {
        switch (paramType.getCanonicalName()) {
            case "net.thevpc.nuts.core.NSession":
            case "net.thevpc.nuts.core.NWorkspace": {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object resolve(Class<?> paramType) {
        switch (paramType.getCanonicalName()) {
            case "net.thevpc.nuts.core.NSession": {
                return NSession.of();
            }
            case "net.thevpc.nuts.core.NWorkspace": {
                return NWorkspace.of();
            }
        }
        return false;
    }
}
