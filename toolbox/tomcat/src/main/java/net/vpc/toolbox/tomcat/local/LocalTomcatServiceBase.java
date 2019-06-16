package net.vpc.toolbox.tomcat.local;

import java.io.PrintStream;

public abstract class LocalTomcatServiceBase {

    public abstract Object getConfig();

    public abstract String getName();

    public abstract LocalTomcatServiceBase write(PrintStream out);

    public abstract LocalTomcatServiceBase remove();
}
