package net.thevpc.nuts.runtime.standalone.dependency;

import net.thevpc.nuts.*;

import java.util.Arrays;
import java.util.function.Function;

public class DefaultNutsDependencies implements NutsDependencies {
    private NutsId[] sourceIds;

    private NutsDependencyFilter filter;

    private NutsDependency[] immediateDependencies;

    private NutsDependency[] nonMergedDependencies;
    private NutsDependencyTreeNode[] nonMergedNodes;
    
    private NutsDependency[] mergedDependencies;
    private NutsDependencyTreeNode[] mergedNodes;
    private transient NutsSession session;
    private transient Function<NutsElements,NutsElement> descr;

    public DefaultNutsDependencies(NutsId[] ids, NutsDependencyFilter filter, NutsDependency[] immediateDependencies, 
            NutsDependency[] nonMergedDependencies, NutsDependencyTreeNode[] nonMergedNodes,
            NutsDependency[] mergedDependencies, NutsDependencyTreeNode[] mergedNodes,
                                   Function<NutsElements,NutsElement> descr,
                                   NutsSession session
    ) {
        this.sourceIds = ids;
        this.filter = filter;
        this.immediateDependencies = immediateDependencies;
        this.nonMergedDependencies = nonMergedDependencies;
        this.nonMergedNodes = nonMergedNodes;
        this.mergedNodes = mergedNodes;
        this.mergedDependencies = mergedDependencies;
        this.session = session;
        this.descr = descr;
    }

    @Override
    public NutsStream<NutsId> sourceIds() {
        return  NutsStream.of(
                NutsIterator.of(Arrays.asList(sourceIds).iterator(),descr)
                ,session);
    }

    @Override
    public NutsDependencyFilter filter() {
        return filter;
    }

    @Override
    public NutsStream<NutsDependency> immediate() {
        return  NutsStream.of(
                NutsIterator.of(Arrays.asList(immediateDependencies).iterator(),descr)
                ,session);
    }

    @Override
    public NutsStream<NutsDependency> all() {
        return  NutsStream.of(
                NutsIterator.of(Arrays.asList(nonMergedDependencies).iterator(),descr)
                ,session);
    }

    @Override
    public NutsStream<NutsDependencyTreeNode> nodes() {
        return  NutsStream.of(
                NutsIterator.of(Arrays.asList(nonMergedNodes).iterator(),descr)
                ,session);
    }

    @Override
    public NutsStream<NutsDependency> mergedDependencies() {
        return  NutsStream.of(
                NutsIterator.of(Arrays.asList(mergedDependencies).iterator(),descr)
                ,session);
    }

    @Override
    public NutsStream<NutsDependencyTreeNode> mergedNodes() {
        return  NutsStream.of(
                NutsIterator.of(Arrays.asList(mergedNodes).iterator(),descr)
                ,session);
    }

    @Override
    public NutsIterator<NutsDependency> iterator() {
        return all().iterator();
    }

    @Override
    public NutsElement describe(NutsElements elems) {
        return descr.apply(elems);
    }
}
