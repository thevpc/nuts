package net.thevpc.nuts.reflect;

import net.thevpc.nuts.concurrent.NScopedValue;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

public interface NReflect extends NComponent {
    static NReflect of() {
        return NExtensions.of(NReflect.class);
    }

    NScopedValue<NBeanContainer> scopedBeanContainer();
}
