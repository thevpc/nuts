package net.vpc.toolbox.tomcat.remote;

import java.io.PrintStream;

public abstract class RemoteTomcatServiceBase {

    public abstract Object getConfig();

    public abstract String getName();

    public abstract RemoteTomcatServiceBase print(PrintStream out);

    public RemoteTomcatServiceBase println(PrintStream out) {
        print(out);
        out.println();
        return this;
    }

    public abstract RemoteTomcatServiceBase remove();
}
