package net.vpc.toolbox.tomcat.remote;

import java.io.PrintStream;

public abstract class RemoteTomcatServiceBase {

    public abstract Object getConfig();

    public abstract String getName();

    public abstract RemoteTomcatServiceBase write(PrintStream out);

    public abstract RemoteTomcatServiceBase remove();
}
