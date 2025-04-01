package net.thevpc.nuts.runtime.standalone.dependency;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NStream;

import java.util.Arrays;

public class DefaultNDependencies implements NDependencies {
    private String solver;
    private NId[] sourceIds;

    private NDependencyFilter filter;

    private NDependency[] immediateDependencies;

    private NDependency[] nonMergedDependencies;
    private NDependencyTreeNode[] nonMergedNodes;

    private NDependency[] mergedDependencies;
    private NDependencyTreeNode[] mergedNodes;
    private transient NEDesc description;

    public DefaultNDependencies(String solver, NId[] ids, NDependencyFilter filter, NDependency[] immediateDependencies,
                                NDependency[] nonMergedDependencies, NDependencyTreeNode[] nonMergedNodes,
                                NDependency[] mergedDependencies, NDependencyTreeNode[] mergedNodes,
                                NEDesc description
    ) {
        this.sourceIds = ids;
        this.solver = solver;
        this.filter = filter;
        this.immediateDependencies = immediateDependencies;
        this.nonMergedDependencies = nonMergedDependencies;
        this.nonMergedNodes = nonMergedNodes;
        this.mergedNodes = mergedNodes;
        this.mergedDependencies = mergedDependencies;
        this.description = description;
    }

    public String solver() {
        return solver;
    }

    @Override
    public NStream<NId> sourceIds() {
        return NStream.of(
                NIterator.of(Arrays.asList(sourceIds).iterator()).withDesc(description)
        );
    }

    @Override
    public NDependencyFilter filter() {
        return filter;
    }

    @Override
    public NStream<NDependency> immediate() {
        return NStream.of(
                NIterator.of(Arrays.asList(immediateDependencies).iterator()).withDesc(description)
        );
    }

    @Override
    public NStream<NDependency> transitive() {
        return NStream.of(
                NIterator.of(Arrays.asList(nonMergedDependencies).iterator()).withDesc(description)
        );
    }

    @Override
    public NStream<NDependencyTreeNode> transitiveNodes() {
        return NStream.of(
                NIterator.of(Arrays.asList(nonMergedNodes).iterator()).withDesc(description)
        );
    }

    @Override
    public NStream<NDependency> transitiveWithSource() {
        return NStream.of(
                NIterator.of(Arrays.asList(mergedDependencies).iterator()).withDesc(description)
        );
    }

    @Override
    public NStream<NDependencyTreeNode> sourceNodes() {
        return NStream.of(
                NIterator.of(Arrays.asList(mergedNodes).iterator()).withDesc(description)
        );
    }

    @Override
    public NIterator<NDependency> iterator() {
        return transitive().iterator();
    }

    @Override
    public NElement describe() {
        return NEDesc.safeDescribe(description);
    }

    @Override
    public NDependencies withDesc(NEDesc description) {
        this.description=description;
        return this;
    }
}
