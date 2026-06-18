package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.internal.rpi.NReflectRPI;
import net.thevpc.nuts.reflect.NClassLoader;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNExtensions;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScore;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNReflectRPI implements NReflectRPI {
    public NClassLoader createClassLoader(String name,ClassLoader parent) {
        return ((DefaultNExtensions) NExtensions.of()).getModel().getNutsURLClassLoader(name,parent);
    }
}
