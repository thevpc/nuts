package net.thevpc.nuts.toolbox.tomcat.remote;

import net.thevpc.nuts.NutsPrintStream;

public abstract class RemoteTomcatServiceBase {

    public abstract Object getConfig();

    public abstract String getName();

    public abstract RemoteTomcatServiceBase print(NutsPrintStream out);

    public RemoteTomcatServiceBase println(NutsPrintStream out) {
        print(out);
        out.println();
        return this;
    }

    public abstract RemoteTomcatServiceBase remove();
}
