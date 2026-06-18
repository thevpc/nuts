package net.thevpc.nuts.internal.rpi;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.reflect.NClassLoader;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;

public interface NReflectRPI extends NComponent {
    static NReflectRPI of() {
        return get().get();
    }
    static NOptional<NReflectRPI> get() {
        return NExtensions.get(NReflectRPI.class);
    }

    NClassLoader createClassLoader(String name, ClassLoader parent);
}
