package net.thevpc.nuts.toolbox.ntomcat.local;

import net.thevpc.nuts.io.NStream;

public abstract class LocalTomcatServiceBase {

    public abstract Object getConfig();

    public abstract String getName();

    public abstract LocalTomcatServiceBase print(NStream out);

    public LocalTomcatServiceBase println(NStream out) {
        print(out);
        out.println();
        return this;
    }

    public abstract LocalTomcatServiceBase remove();
}
