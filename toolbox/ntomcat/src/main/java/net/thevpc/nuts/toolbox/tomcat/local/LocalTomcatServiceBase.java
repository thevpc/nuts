package net.thevpc.nuts.toolbox.tomcat.local;

import java.io.PrintStream;

public abstract class LocalTomcatServiceBase {

    public abstract Object getConfig();

    public abstract String getName();

    public abstract LocalTomcatServiceBase print(PrintStream out);

    public LocalTomcatServiceBase println(PrintStream out) {
        print(out);
        out.println();
        return this;
    }

    public abstract LocalTomcatServiceBase remove();
}
