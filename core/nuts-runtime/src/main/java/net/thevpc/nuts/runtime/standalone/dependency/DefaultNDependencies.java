package net.thevpc.nuts.runtime.standalone.dependency;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NStream;

import java.util.Arrays;
import java.util.function.Function;

public class DefaultNDependencies implements NDependencies {
    private String solver;
    private NId[] sourceIds;

    private NDependencyFilter filter;

    private NDependency[] immediateDependencies;

    private NDependency[] nonMergedDependencies;
    private NDependencyTreeNode[] nonMergedNodes;

    private NDependency[] mergedDependencies;
    private NDependencyTreeNode[] mergedNodes;
    private transient NSession session;
    private transient Function<NSession, NElement> descr;

    public DefaultNDependencies(String solver, NId[] ids, NDependencyFilter filter, NDependency[] immediateDependencies,
                                NDependency[] nonMergedDependencies, NDependencyTreeNode[] nonMergedNodes,
                                NDependency[] mergedDependencies, NDependencyTreeNode[] mergedNodes,
                                Function<NSession, NElement> descr,
                                NSession session
    ) {
        this.sourceIds = ids;
        this.solver = solver;
        this.filter = filter;
        this.immediateDependencies = immediateDependencies;
        this.nonMergedDependencies = nonMergedDependencies;
        this.nonMergedNodes = nonMergedNodes;
        this.mergedNodes = mergedNodes;
        this.mergedDependencies = mergedDependencies;
        this.session = session;
        this.descr = descr;
    }

    public String solver() {
        return solver;
    }

    @Override
    public NStream<NId> sourceIds() {
        return NStream.of(
                NIterator.of(Arrays.asList(sourceIds).iterator(), descr)
                , session);
    }

    @Override
    public NDependencyFilter filter() {
        return filter;
    }

    @Override
    public NStream<NDependency> immediate() {
        return NStream.of(
                NIterator.of(Arrays.asList(immediateDependencies).iterator(), descr)
                , session);
    }

    @Override
    public NStream<NDependency> transitive() {
        return NStream.of(
                NIterator.of(Arrays.asList(nonMergedDependencies).iterator(), descr)
                , session);
    }

    @Override
    public NStream<NDependencyTreeNode> transitiveNodes() {
        return NStream.of(
                NIterator.of(Arrays.asList(nonMergedNodes).iterator(), descr)
                , session);
    }

    @Override
    public NStream<NDependency> transitiveWithSource() {
        return NStream.of(
                NIterator.of(Arrays.asList(mergedDependencies).iterator(), descr)
                , session);
    }

    @Override
    public NStream<NDependencyTreeNode> sourceNodes() {
        return NStream.of(
                NIterator.of(Arrays.asList(mergedNodes).iterator(), descr)
                , session);
    }

    @Override
    public NIterator<NDependency> iterator() {
        return transitive().iterator();
    }

    @Override
    public NElement describe(NSession session) {
        return descr.apply(session);
    }
}
