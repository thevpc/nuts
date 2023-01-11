package net.thevpc.nuts.toolbox.ntomcat.local;

import net.thevpc.nuts.io.NOutputStream;

public abstract class LocalTomcatServiceBase {

    public abstract Object getConfig();

    public abstract String getName();

    public abstract LocalTomcatServiceBase print(NOutputStream out);

    public LocalTomcatServiceBase println(NOutputStream out) {
        print(out);
        out.println();
        return this;
    }

    public abstract LocalTomcatServiceBase remove();
}
