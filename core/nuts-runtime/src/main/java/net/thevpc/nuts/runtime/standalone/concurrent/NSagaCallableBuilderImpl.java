package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.reflect.NBeanContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class NSagaCallableBuilderImpl implements NSagaCallableBuilder {
    private final AtomicInteger idCounter = new AtomicInteger(1);
    private final NSagaStore store;
    private final List<NSagaNodeModel> roots=new ArrayList<>();

    public NSagaCallableBuilderImpl(NSagaStore store) {
        this.store = store;
    }

    @Override
    public Suite<NSagaCallableBuilder> start() {
        return new SuiteImpl<>(this, roots, this);
    }

    @Override
    public NSagaCallable build() {
        NSagaModel saga = new NSagaModel();
        if(!roots.isEmpty()) {
            if(roots.size()==1) {
                saga.setNode(roots.get(0));
            }else{
                NSagaNodeModel m = new NSagaNodeModel();
                m.setType(NSagaNodeType.SUITE);
                m.setId(UUID.randomUUID().toString());
                m.setCompensationStrategy(NCompensationStrategy.ABORT);
                m.setName("<root>");
                m.setChildren(roots.stream().map(x->x.copy()).collect(Collectors.toList()));
                saga.setNode(m);
            }
        }
        return new NSagaCallableImpl(saga.clone(),store);
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
                    .setId(nextId())
                    .setName(name)
                    .setStepCall(step)
                    .setType(NSagaNodeType.STEP);
            currentNodes.add(node);
            return this;
        }

        @Override
        public If<Suite<P>> thenIf(String name, NSagaCondition condition) {
            NSagaNodeModel node = new NSagaNodeModel()
                    .setId(nextId())
                    .setName(name)
                    .setStepCondition(condition)
                    .setType(NSagaNodeType.IF);
            currentNodes.add(node);
            return new IfImpl<>(this, node);
        }

        @Override
        public While<Suite<P>> thenWhile(String name, NSagaCondition condition) {
            NSagaNodeModel node = new NSagaNodeModel()
                    .setId(nextId())
                    .setName(name)
                    .setStepCondition(condition)
                    .setType(NSagaNodeType.WHILE);
            currentNodes.add(node);
            return new WhileImpl<>(this, node.getChildren(), node);
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
            this.currentNodes = ifNode.getChildren(); // initially the IF main branch
        }

        @Override
        public If<P> then(String name, NSagaStep step) {
            NSagaNodeModel node = new NSagaNodeModel()
                    .setId(nextId())
                    .setName(name)
                    .setStepCall(step)
                    .setType(NSagaNodeType.STEP);
            currentNodes.add(node);
            return this;
        }


        @Override
        public If<P> elseIf(String name, NSagaCondition condition) {
            NSagaNodeModel node = new NSagaNodeModel()
                    .setId(nextId())
                    .setName(name)
                    .setStepCondition(condition)
                    .setType(NSagaNodeType.SUITE);

            ifNode.getElseIfBranches().add(node);

            // change currentNodes to point to the new else-if branch
            this.currentNodes = node.getChildren();

            // return self, developer can chain then(...)
            return this;
        }

        @Override
        public If<P> otherwise() {
            NSagaNodeModel node = new NSagaNodeModel()
                    .setId(nextId())
                    .setName("otherwise")
                    .setType(NSagaNodeType.SUITE);

            ifNode.getOtherwiseBranch().add(node);

            // change currentNodes to point to otherwise branch
            this.currentNodes = node.getChildren();

            return this;
        }

        @Override
        public P end() {
            return parent;
        }

        @Override
        public If<If<P>> thenIf(String name, NSagaCondition condition) {
            NSagaNodeModel node = new NSagaNodeModel()
                    .setId(nextId())
                    .setName(name)
                    .setStepCondition(condition)
                    .setType(NSagaNodeType.IF);
            currentNodes.add(node);
            return new IfImpl<If<P>>(this,node);
        }

        @Override
        public While<If<P>> thenWhile(String name, NSagaCondition condition) {
            NSagaNodeModel node = new NSagaNodeModel()
                    .setId(nextId())
                    .setName(name)
                    .setStepCondition(condition)
                    .setType(NSagaNodeType.WHILE);
            currentNodes.add(node);
            return new WhileImpl<>(this, node.getChildren(), node);
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
                    .setId(nextId())
                    .setName(name)
                    .setStepCall(step)
                    .setType(NSagaNodeType.STEP);
            currentNodes.add(node);
            return this;
        }

        @Override
        public If<While<P>> thenIf(String name, NSagaCondition condition) {
            NSagaNodeModel node = new NSagaNodeModel()
                    .setId(nextId())
                    .setName(name)
                    .setStepCondition(condition)
                    .setType(NSagaNodeType.IF);
            currentNodes.add(node);
            return new IfImpl<>(this, node);
        }

        @Override
        public While<While<P>> thenWhile(String name, NSagaCondition condition) {
            NSagaNodeModel node = new NSagaNodeModel()
                    .setId(nextId())
                    .setName(name)
                    .setStepCondition(condition)
                    .setType(NSagaNodeType.WHILE);
            currentNodes.add(node);
            return new WhileImpl<>(this, node.getChildren(), node);
        }

        @Override
        public P end() {
            return parent;
        }
    }
}
