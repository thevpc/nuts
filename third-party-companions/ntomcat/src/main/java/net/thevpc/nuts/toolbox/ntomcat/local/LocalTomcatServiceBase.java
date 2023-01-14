package net.thevpc.nuts.toolbox.ntomcat.local;

import net.thevpc.nuts.io.NPrintStream;

public abstract class LocalTomcatServiceBase {

    public abstract Object getConfig();

    public abstract String getName();

    public abstract LocalTomcatServiceBase print(NPrintStream out);

    public LocalTomcatServiceBase println(NPrintStream out) {
        print(out);
        out.println();
        return this;
    }

    public abstract LocalTomcatServiceBase remove();
}
