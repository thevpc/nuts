package net.vpc.app.nuts;

import java.io.PrintStream;

public interface NutsTreeFormat<T> {

    public NutsTreeNodeFormatter getFormatter();

    public NutsTreeFormat setFormatter(NutsTreeNodeFormatter formatter);

    public NutsTreeLinkFormatter getLinkFormatter();

    public NutsTreeFormat setLinkFormatter(NutsTreeLinkFormatter linkFormatter);

    public NutsTreeModel getTree();

    public NutsTreeFormat setTree(NutsTreeModel tree);

    public void print(PrintStream out);

    public boolean configure(NutsCommandLine cmdLine);
    
    
}
