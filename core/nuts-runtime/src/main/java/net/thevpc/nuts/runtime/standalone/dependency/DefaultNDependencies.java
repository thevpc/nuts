package net.thevpc.nuts.runtime.standalone.dependency;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NStream;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class DefaultNDependencies implements NDependencies {
    private String solver;
    private NId[] sourceIds;

    private NDependencyFilter filter;

    private NDependency[] immediateDependencies;

    private NDependency[] nonMergedDependencies;
    private NDependencyTreeNode[] nonMergedNodes;

    private NDependency[] mergedDependencies;
    private NDependencyTreeNode[] mergedNodes;
    private transient Supplier<NElement> description;

    public DefaultNDependencies(String solver, NId[] ids, NDependencyFilter filter, NDependency[] immediateDependencies,
                                NDependency[] nonMergedDependencies, NDependencyTreeNode[] nonMergedNodes,
                                NDependency[] mergedDependencies, NDependencyTreeNode[] mergedNodes,
                                Supplier<NElement> description
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
        return NStream.ofIterator(
                NIterator.of(Arrays.asList(sourceIds).iterator()).redescribe(description)
        );
    }

    @Override
    public NDependencyFilter filter() {
        return filter;
    }

    @Override
    public NStream<NDependency> immediate() {
        return NStream.ofIterator(
                NIterator.of(Arrays.asList(immediateDependencies).iterator()).redescribe(description)
        );
    }

    @Override
    public NStream<NDependency> transitive() {
        return NStream.ofIterator(
                NIterator.of(Arrays.asList(nonMergedDependencies).iterator()).redescribe(description)
        );
    }

    @Override
    public NStream<NDependencyTreeNode> transitiveNodes() {
        return NStream.ofIterator(
                NIterator.of(Arrays.asList(nonMergedNodes).iterator()).redescribe(description)
        );
    }

    @Override
    public NStream<NDependency> transitiveWithSource() {
        return NStream.ofIterator(
                NIterator.of(Arrays.asList(mergedDependencies).iterator()).redescribe(description)
        );
    }

    @Override
    public NStream<NDependencyTreeNode> sourceNodes() {
        return NStream.ofIterator(
                NIterator.of(Arrays.asList(mergedNodes).iterator()).redescribe(description)
        );
    }

    @Override
    public List<NDependency> toList() {
        return transitive().toList();
    }

    @Override
    public NIterator<NDependency> iterator() {
        return transitive().iterator();
    }

    @Override
    public NElement describe() {
        return NDescribables.safeDescribe(description);
    }

    @Override
    public NDependencies redescribe(Supplier<NElement> description) {
        this.description=description;
        return this;
    }
}
