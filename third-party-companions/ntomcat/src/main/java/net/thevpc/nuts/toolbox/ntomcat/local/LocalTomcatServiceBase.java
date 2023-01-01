package net.thevpc.nuts.toolbox.ntomcat.local;

import net.thevpc.nuts.io.NOutStream;

public abstract class LocalTomcatServiceBase {

    public abstract Object getConfig();

    public abstract String getName();

    public abstract LocalTomcatServiceBase print(NOutStream out);

    public LocalTomcatServiceBase println(NOutStream out) {
        print(out);
        out.println();
        return this;
    }

    public abstract LocalTomcatServiceBase remove();
}
