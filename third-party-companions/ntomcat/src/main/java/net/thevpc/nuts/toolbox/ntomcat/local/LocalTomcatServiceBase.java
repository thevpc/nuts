package net.thevpc.nuts.toolbox.ntomcat.local;

import net.thevpc.nuts.io.NutsPrintStream;

public abstract class LocalTomcatServiceBase {

    public abstract Object getConfig();

    public abstract String getName();

    public abstract LocalTomcatServiceBase print(NutsPrintStream out);

    public LocalTomcatServiceBase println(NutsPrintStream out) {
        print(out);
        out.println();
        return this;
    }

    public abstract LocalTomcatServiceBase remove();
}
