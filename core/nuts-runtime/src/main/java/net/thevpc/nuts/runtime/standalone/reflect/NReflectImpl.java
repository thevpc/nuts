package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NScopedValue;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.reflect.NReflect;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NSupportLevelContext;

public class NReflectImpl implements NReflect {

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NScopedValue<NBeanContainer> scopedBeanContainer() {
        return NWorkspaceExt.of().getModel().scopedBeanContainer;
    }
}
