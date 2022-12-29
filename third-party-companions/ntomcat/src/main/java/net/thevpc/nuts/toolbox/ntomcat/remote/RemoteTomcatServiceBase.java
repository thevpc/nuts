package net.thevpc.nuts.toolbox.ntomcat.remote;

import net.thevpc.nuts.io.NStream;

public abstract class RemoteTomcatServiceBase {

    public abstract Object getConfig();

    public abstract String getName();

    public abstract RemoteTomcatServiceBase print(NStream out);

    public RemoteTomcatServiceBase println(NStream out) {
        print(out);
        out.println();
        return this;
    }

    public abstract RemoteTomcatServiceBase remove();
}
