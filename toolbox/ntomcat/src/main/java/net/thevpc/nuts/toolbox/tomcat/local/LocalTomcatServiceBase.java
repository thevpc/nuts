package net.thevpc.nuts.toolbox.tomcat.local;

import net.thevpc.nuts.NutsPrintStream;

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
