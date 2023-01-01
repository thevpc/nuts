package net.thevpc.nuts.toolbox.ntomcat.remote;

import net.thevpc.nuts.io.NOutStream;

public abstract class RemoteTomcatServiceBase {

    public abstract Object getConfig();

    public abstract String getName();

    public abstract RemoteTomcatServiceBase print(NOutStream out);

    public RemoteTomcatServiceBase println(NOutStream out) {
        print(out);
        out.println();
        return this;
    }

    public abstract RemoteTomcatServiceBase remove();
}
