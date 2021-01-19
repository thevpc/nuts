package net.thevpc.nuts.runtime.core.model;

import net.thevpc.nuts.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class DefaultNutsDependencies implements NutsDependencies {
    private NutsId[] ids;

    private NutsDependencyFilter filter;

    private NutsDependency[] immediate;

    private NutsDependency[] all;
    private NutsDependencyTreeNode[] nodes;

    public DefaultNutsDependencies(NutsId[] ids, NutsDependencyFilter filter, NutsDependency[] immediate, NutsDependency[] all, NutsDependencyTreeNode[] nodes) {
        this.ids = ids;
        this.filter = filter;
        this.immediate = immediate;
        this.all = all;
        this.nodes = nodes;
    }

    public Stream<NutsDependency> stream(){
        return all().stream();
    }

    @Override
    public List<NutsId> ids() {
        return Arrays.asList(ids);
    }

    @Override
    public NutsDependencyFilter filter() {
        return filter;
    }

    @Override
    public List<NutsDependency> immediate() {
        return Arrays.asList(immediate);
    }

    @Override
    public List<NutsDependency> all() {
        return Arrays.asList(all);
    }

    @Override
    public List<NutsDependencyTreeNode> nodes() {
        return Arrays.asList(nodes);
    }

    @Override
    public Iterator<NutsDependency> iterator() {
        return Arrays.asList(all).iterator();
    }
}
