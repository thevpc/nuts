package net.thevpc.nuts.reflect;

import net.thevpc.nuts.concurrent.NScopedStack;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;

public interface NReflect extends NComponent {
    static NReflect of() {
        return NExtensions.of(NReflect.class);
    }

    NScopedStack<NBeanContainer> scopedBeanContainerStack();
    NBeanContainer scopedBeanContainer();
}
