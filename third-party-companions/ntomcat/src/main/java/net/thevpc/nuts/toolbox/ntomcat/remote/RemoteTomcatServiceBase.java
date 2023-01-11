package net.thevpc.nuts.toolbox.ntomcat.remote;

import net.thevpc.nuts.io.NOutputStream;

public abstract class RemoteTomcatServiceBase {

    public abstract Object getConfig();

    public abstract String getName();

    public abstract RemoteTomcatServiceBase print(NOutputStream out);

    public RemoteTomcatServiceBase println(NOutputStream out) {
        print(out);
        out.println();
        return this;
    }

    public abstract RemoteTomcatServiceBase remove();
}
