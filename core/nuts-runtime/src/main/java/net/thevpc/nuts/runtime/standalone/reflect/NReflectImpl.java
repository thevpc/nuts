package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.concurrent.NScopedStack;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.reflect.NReflect;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.util.NScorableContext;

public class NReflectImpl implements NReflect {

    @Override
    public int getScore(NScorableContext context) {
        return DEFAULT_SCORE;
    }

    @Override
    public NScopedStack<NBeanContainer> scopedBeanContainerStack() {
        return NWorkspaceExt.of().getModel().scopedBeanContainerStack;
    }

    @Override
    public NBeanContainer scopedBeanContainer() {
        return NWorkspaceExt.of().getModel().scopedBeanContainer;
    }
}
