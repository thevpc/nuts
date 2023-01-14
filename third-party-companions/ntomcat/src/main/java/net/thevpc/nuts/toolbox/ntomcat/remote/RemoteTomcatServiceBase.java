package net.thevpc.nuts.toolbox.ntomcat.remote;

import net.thevpc.nuts.io.NPrintStream;

public abstract class RemoteTomcatServiceBase {

    public abstract Object getConfig();

    public abstract String getName();

    public abstract RemoteTomcatServiceBase print(NPrintStream out);

    public RemoteTomcatServiceBase println(NPrintStream out) {
        print(out);
        out.println();
        return this;
    }

    public abstract RemoteTomcatServiceBase remove();
}
