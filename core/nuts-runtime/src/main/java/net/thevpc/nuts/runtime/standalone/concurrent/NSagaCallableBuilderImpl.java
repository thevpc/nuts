package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class NSagaCallableBuilderImpl implements NSagaCallableBuilder {
    private String id;
    private NSagaStore store;
    private final AtomicInteger idCounter = new AtomicInteger(1);
    private final List<NSagaNodeModel> roots = new ArrayList<>();

    public NSagaCallableBuilderImpl(NSagaStore store) {
        this(null, store);
    }

    public NSagaCallableBuilderImpl(String id, NSagaStore store) {
        this.id = id;
        this.store = store;
    }

    @Override
    public NSagaCallableBuilder id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public NSagaCallableBuilder store(NSagaStore store) {
        this.store = store;
        return this;
    }

    @Override
    public NSagaStore store() {
        return store;
    }

    @Override
    public Suite<NSagaCallableBuilder> start() {
        return new SuiteImpl<>(this, roots, this);
    }

    @Override
    public NSagaCallable build() {
        String sagaId = id != null && !id.trim().isEmpty() ? id : UUID.randomUUID().toString();
        NSagaModel saga = new NSagaModel();
        saga.id(sagaId);

        if (!roots.isEmpty()) {
            if (roots.size() == 1) {
                saga.node(roots.get(0));
            } else {
                NSagaNodeModel m = new NSagaNodeModel();
                m.type(NSagaNodeType.SUITE);
                m.id(sagaId + "-root");
                m.compensationStrategy(NCompensationStrategy.ABORT);
                m.name("<root>");
                m.children(roots.stream().map(x -> x.copy()).collect(Collectors.toList()));
                saga.node(m);
            }
        }

        if (store != null) {
            NSagaModel existing = store.load(sagaId);
            if (existing != null && existing.context() != null) {
                saga.context(existing.context().clone());
                restoreNodeStatuses(saga.node(), existing.node());
                return new NSagaCallableImpl(saga, store);
            }
        }
        return new NSagaCallableImpl(saga.clone(), store);
    }

    private void restoreNodeStatuses(NSagaNodeModel current, NSagaNodeModel existing) {
        if (current == null || existing == null) {
            return;
        }
        Map<String, NSagaNodeModel> existingNodesById = new HashMap<>();
        collectNodes(existing, existingNodesById);
        applyStatuses(current, existingNodesById);
    }

    private void collectNodes(NSagaNodeModel node, Map<String, NSagaNodeModel> map) {
        if (node == null) return;
        if (node.id() != null) map.put(node.id(), node);
        if (node.children() != null) {
            for (NSagaNodeModel c : node.children()) collectNodes(c, map);
        }
        if (node.elseIfBranches() != null) {
            for (NSagaNodeModel c : node.elseIfBranches()) collectNodes(c, map);
        }
        if (node.otherwiseBranch() != null) {
            for (NSagaNodeModel c : node.otherwiseBranch()) collectNodes(c, map);
        }
    }

    private void applyStatuses(NSagaNodeModel node, Map<String, NSagaNodeModel> map) {
        if (node == null) return;
        NSagaNodeModel existing = map.get(node.id());
        if (existing != null && existing.status() != null) {
            node.status(existing.status());
        }
        if (node.children() != null) {
            for (NSagaNodeModel c : node.children()) applyStatuses(c, map);
        }
        if (node.elseIfBranches() != null) {
            for (NSagaNodeModel c : node.elseIfBranches()) applyStatuses(c, map);
        }
        if (node.otherwiseBranch() != null) {
            for (NSagaNodeModel c : node.otherwiseBranch()) applyStatuses(c, map);
        }
    }

    private String nextId() {
        return "node-" + idCounter.getAndIncrement();
    }

    // -------------------------
    // Suite Implementation
    // -------------------------
    private class SuiteImpl<P> implements NSagaCallableBuilder.Suite<P> {
        private final P parent;
        private final List<NSagaNodeModel> currentNodes;
        private final NSagaCallableBuilder builder;

        SuiteImpl(P parent, List<NSagaNodeModel> currentNodes, NSagaCallableBuilder builder) {
            this.parent = parent;
            this.currentNodes = currentNodes;
            this.builder = builder;
        }

        private String nextId() {
            return "node-" + idCounter.getAndIncrement();
        }

        @Override
        public Suite<P> then(String name, NSagaStep step) {
            NSagaNodeModel node = new NSagaNodeModel()
                    .id(nextId())
                    .name(name)
                    .stepCall(step)
                    .type(NSagaNodeType.STEP);
            currentNodes.add(node);
            return this;
        }

        @Override
        public If<Suite<P>> thenIf(String name, NSagaCondition condition) {
            NSagaNodeModel node = new NSagaNodeModel()
                    .id(nextId())
                    .name(name)
                    .stepCondition(condition)
                    .type(NSagaNodeType.IF);
            currentNodes.add(node);
            return new IfImpl<>(this, node);
        }

        @Override
        public While<Suite<P>> thenWhile(String name, NSagaCondition condition) {
            NSagaNodeModel node = new NSagaNodeModel()
                    .id(nextId())
                    .name(name)
                    .stepCondition(condition)
                    .type(NSagaNodeType.WHILE);
            currentNodes.add(node);
            return new WhileImpl<>(this, node.children(), node);
        }

        @Override
        public P end() {
            return parent;
        }
    }

    // -------------------------
    // If Implementation
    // -------------------------
    private class IfImpl<P> implements If<P> {
        private final P parent;
        private final NSagaNodeModel ifNode;

        // track which branch we are currently adding steps to
        private List<NSagaNodeModel> currentNodes;

        public IfImpl(P parent, NSagaNodeModel ifNode) {
            this.parent = parent;
            this.ifNode = ifNode;
            this.currentNodes = ifNode.children(); // initially the IF main branch
        }

        @Override
        public If<P> then(String name, NSagaStep step) {
            NSagaNodeModel node = new NSagaNodeModel()
                    .id(nextId())
                    .name(name)
                    .stepCall(step)
                    .type(NSagaNodeType.STEP);
            currentNodes.add(node);
            return this;
        }

        @Override
        public If<P> elseIf(String name, NSagaCondition condition) {
            NSagaNodeModel node = new NSagaNodeModel()
                    .id(nextId())
                    .name(name)
                    .stepCondition(condition)
                    .type(NSagaNodeType.SUITE);

            ifNode.elseIfBranches().add(node);

            // change currentNodes to point to the new else-if branch
            this.currentNodes = node.children();

            // return self, developer can chain then(...)
            return this;
        }

        @Override
        public If<P> otherwise() {
            NSagaNodeModel node = new NSagaNodeModel()
                    .id(nextId())
                    .name("otherwise")
                    .type(NSagaNodeType.SUITE);

            ifNode.otherwiseBranch().add(node);

            // change currentNodes to point to otherwise branch
            this.currentNodes = node.children();

            return this;
        }

        @Override
        public P end() {
            return parent;
        }

        @Override
        public If<If<P>> thenIf(String name, NSagaCondition condition) {
            NSagaNodeModel node = new NSagaNodeModel()
                    .id(nextId())
                    .name(name)
                    .stepCondition(condition)
                    .type(NSagaNodeType.IF);
            currentNodes.add(node);
            return new IfImpl<If<P>>(this, node);
        }

        @Override
        public While<If<P>> thenWhile(String name, NSagaCondition condition) {
            NSagaNodeModel node = new NSagaNodeModel()
                    .id(nextId())
                    .name(name)
                    .stepCondition(condition)
                    .type(NSagaNodeType.WHILE);
            currentNodes.add(node);
            return new WhileImpl<>(this, node.children(), node);
        }
    }

    // -------------------------
    // While Implementation
    // -------------------------
    private class WhileImpl<P> implements NSagaCallableBuilder.While<P> {
        private final P parent;
        private final List<NSagaNodeModel> currentNodes;
        private final NSagaNodeModel whileNode;

        WhileImpl(P parent, List<NSagaNodeModel> currentNodes, NSagaNodeModel whileNode) {
            this.parent = parent;
            this.currentNodes = currentNodes;
            this.whileNode = whileNode;
        }

        private String nextId() {
            return "node-" + idCounter.getAndIncrement();
        }

        @Override
        public While<P> then(String name, NSagaStep step) {
            NSagaNodeModel node = new NSagaNodeModel()
                    .id(nextId())
                    .name(name)
                    .stepCall(step)
                    .type(NSagaNodeType.STEP);
            currentNodes.add(node);
            return this;
        }

        @Override
        public If<While<P>> thenIf(String name, NSagaCondition condition) {
            NSagaNodeModel node = new NSagaNodeModel()
                    .id(nextId())
                    .name(name)
                    .stepCondition(condition)
                    .type(NSagaNodeType.IF);
            currentNodes.add(node);
            return new IfImpl<>(this, node);
        }

        @Override
        public While<While<P>> thenWhile(String name, NSagaCondition condition) {
            NSagaNodeModel node = new NSagaNodeModel()
                    .id(nextId())
                    .name(name)
                    .stepCondition(condition)
                    .type(NSagaNodeType.WHILE);
            currentNodes.add(node);
            return new WhileImpl<>(this, node.children(), node);
        }

        @Override
        public P end() {
            return parent;
        }
    }
}
