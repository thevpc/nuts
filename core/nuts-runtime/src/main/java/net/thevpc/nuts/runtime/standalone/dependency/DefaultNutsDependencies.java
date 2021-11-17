package net.thevpc.nuts.runtime.standalone.dependency;

import net.thevpc.nuts.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class DefaultNutsDependencies implements NutsDependencies {
    private NutsId[] sourceIds;

    private NutsDependencyFilter filter;

    private NutsDependency[] immediateDependencies;

    private NutsDependency[] nonMergedDependencies;
    private NutsDependencyTreeNode[] nonMergedNodes;
    
    private NutsDependency[] mergedDependencies;
    private NutsDependencyTreeNode[] mergedNodes;

    public DefaultNutsDependencies(NutsId[] ids, NutsDependencyFilter filter, NutsDependency[] immediateDependencies, 
            NutsDependency[] nonMergedDependencies, NutsDependencyTreeNode[] nonMergedNodes,
            NutsDependency[] mergedDependencies, NutsDependencyTreeNode[] mergedNodes
    ) {
        this.sourceIds = ids;
        this.filter = filter;
        this.immediateDependencies = immediateDependencies;
        this.nonMergedDependencies = nonMergedDependencies;
        this.nonMergedNodes = nonMergedNodes;
        this.mergedNodes = mergedNodes;
        this.mergedDependencies = mergedDependencies;
    }

    @Override
    public Stream<NutsDependency> stream(){
        return all().stream();
    }

    @Override
    public List<NutsId> sourceIds() {
        return Arrays.asList(sourceIds);
    }

    @Override
    public NutsDependencyFilter filter() {
        return filter;
    }

    @Override
    public List<NutsDependency> immediate() {
        return Arrays.asList(immediateDependencies);
    }

    @Override
    public List<NutsDependency> all() {
        return Arrays.asList(nonMergedDependencies);
    }

    @Override
    public List<NutsDependencyTreeNode> nodes() {
        return Arrays.asList(nonMergedNodes);
    }

    @Override
    public List<NutsDependency> mergedDependencies() {
        return  Arrays.asList(mergedDependencies);
    }

    @Override
    public List<NutsDependencyTreeNode> mergedNodes() {
        return  Arrays.asList(mergedNodes);
    }


    @Override
    public Iterator<NutsDependency> iterator() {
        return all().iterator();
    }
}
