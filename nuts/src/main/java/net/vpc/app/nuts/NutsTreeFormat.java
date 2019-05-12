package net.vpc.app.nuts;

import java.io.PrintStream;
import java.io.Writer;

public interface NutsTreeFormat<T> {

    public NutsTreeNodeFormat getNodeFormat();

    public NutsTreeFormat setNodeFormat(NutsTreeNodeFormat nodeFormat);

    public NutsTreeLinkFormatter getLinkFormat();

    public NutsTreeFormat setLinkFormat(NutsTreeLinkFormatter linkFormat);

    public NutsTreeModel getTree();

    public NutsTreeFormat setTree(NutsTreeModel tree);

    public void print(PrintStream out);

    public void print(Writer out);

    public boolean configure(NutsCommandLine cmdLine, boolean skipIgnored);

    public boolean configureFirst(NutsCommandLine cmdLine);

}
