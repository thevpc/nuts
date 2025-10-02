package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.reflect.NBeanContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class NSagaBuilderImpl implements NSagaBuilder{
    private final AtomicInteger idCounter = new AtomicInteger(1);
    private final NSagaCallStore store;
    private final NBeanContainer beanContainer;
    private final List<NSagaCallNodeModel> roots=new ArrayList<>();

    public NSagaBuilderImpl(NSagaCallStore store, NBeanContainer beanContainer) {
        this.store = store;
        this.beanContainer = beanContainer;
    }

    @Override
    public Suite<NSagaBuilder> start() {
        return new SuiteImpl<>(this, roots, this);
    }

    @Override
    public NSagaCall build() {
        NSagaCallModel saga = new NSagaCallModel();
        if(!roots.isEmpty()) {
            if(roots.size()==1) {
                saga.setNode(roots.get(0));
            }else{
                NSagaCallNodeModel m = new NSagaCallNodeModel();
                m.setType(NSagaCallNodeType.SUITE);
                m.setId(UUID.randomUUID().toString());
                m.setCompensationStrategy(NCompensationStrategy.ABORT);
                m.setName("<root>");
                m.setChildren(roots.stream().map(x->x.copy()).collect(Collectors.toList()));
                saga.setNode(m);
            }
        }
        return new NSagaCallImpl(saga.clone(),store, beanContainer);
    }

    private String nextId() {
        return "node-" + idCounter.getAndIncrement();
    }
    // -------------------------
    // Suite Implementation
    // -------------------------
    private class SuiteImpl<P> implements NSagaBuilder.Suite<P> {
        private final P parent;
        private final List<NSagaCallNodeModel> currentNodes;
        private final NSagaBuilder builder;

        SuiteImpl(P parent, List<NSagaCallNodeModel> currentNodes, NSagaBuilder builder) {
            this.parent = parent;
            this.currentNodes = currentNodes;
            this.builder = builder;
        }

        private String nextId() {
            return "node-" + idCounter.getAndIncrement();
        }

        @Override
        public Suite<P> then(String name, NSagaCallStep step) {
            NSagaCallNodeModel node = new NSagaCallNodeModel()
                    .setId(nextId())
                    .setName(name)
                    .setStepCall(step)
                    .setType(NSagaCallNodeType.STEP);
            currentNodes.add(node);
            return this;
        }

        @Override
        public If<Suite<P>> thenIf(String name, NSagaCallCondition condition) {
            NSagaCallNodeModel node = new NSagaCallNodeModel()
                    .setId(nextId())
                    .setName(name)
                    .setStepCondition(condition)
                    .setType(NSagaCallNodeType.IF);
            currentNodes.add(node);
            return new IfImpl<>(this, node);
        }

        @Override
        public While<Suite<P>> thenWhile(String name, NSagaCallCondition condition) {
            NSagaCallNodeModel node = new NSagaCallNodeModel()
                    .setId(nextId())
                    .setName(name)
                    .setStepCondition(condition)
                    .setType(NSagaCallNodeType.WHILE);
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
        private final NSagaCallNodeModel ifNode;

        // track which branch we are currently adding steps to
        private List<NSagaCallNodeModel> currentNodes;

        public IfImpl(P parent, NSagaCallNodeModel ifNode) {
            this.parent = parent;
            this.ifNode = ifNode;
            this.currentNodes = ifNode.getChildren(); // initially the IF main branch
        }

        @Override
        public If<P> then(String name, NSagaCallStep step) {
            NSagaCallNodeModel node = new NSagaCallNodeModel()
                    .setId(nextId())
                    .setName(name)
                    .setStepCall(step)
                    .setType(NSagaCallNodeType.STEP);
            currentNodes.add(node);
            return this;
        }


        @Override
        public If<P> elseIf(String name, NSagaCallCondition condition) {
            NSagaCallNodeModel node = new NSagaCallNodeModel()
                    .setId(nextId())
                    .setName(name)
                    .setStepCondition(condition)
                    .setType(NSagaCallNodeType.SUITE);

            ifNode.getElseIfBranches().add(node);

            // change currentNodes to point to the new else-if branch
            this.currentNodes = node.getChildren();

            // return self, developer can chain then(...)
            return this;
        }

        @Override
        public If<P> otherwise() {
            NSagaCallNodeModel node = new NSagaCallNodeModel()
                    .setId(nextId())
                    .setName("otherwise")
                    .setType(NSagaCallNodeType.SUITE);

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
        public If<If<P>> thenIf(String name, NSagaCallCondition condition) {
            NSagaCallNodeModel node = new NSagaCallNodeModel()
                    .setId(nextId())
                    .setName(name)
                    .setStepCondition(condition)
                    .setType(NSagaCallNodeType.IF);
            currentNodes.add(node);
            return new IfImpl<If<P>>(this,node);
        }

        @Override
        public While<If<P>> thenWhile(String name, NSagaCallCondition condition) {
            NSagaCallNodeModel node = new NSagaCallNodeModel()
                    .setId(nextId())
                    .setName(name)
                    .setStepCondition(condition)
                    .setType(NSagaCallNodeType.WHILE);
            currentNodes.add(node);
            return new WhileImpl<>(this, node.getChildren(), node);
        }

    }

    // -------------------------
    // While Implementation
    // -------------------------
    private class WhileImpl<P> implements NSagaBuilder.While<P> {
        private final P parent;
        private final List<NSagaCallNodeModel> currentNodes;
        private final NSagaCallNodeModel whileNode;

        WhileImpl(P parent, List<NSagaCallNodeModel> currentNodes, NSagaCallNodeModel whileNode) {
            this.parent = parent;
            this.currentNodes = currentNodes;
            this.whileNode = whileNode;
        }

        private String nextId() {
            return "node-" + idCounter.getAndIncrement();
        }

        @Override
        public While<P> then(String name, NSagaCallStep step) {
            NSagaCallNodeModel node = new NSagaCallNodeModel()
                    .setId(nextId())
                    .setName(name)
                    .setStepCall(step)
                    .setType(NSagaCallNodeType.STEP);
            currentNodes.add(node);
            return this;
        }

        @Override
        public If<While<P>> thenIf(String name, NSagaCallCondition condition) {
            NSagaCallNodeModel node = new NSagaCallNodeModel()
                    .setId(nextId())
                    .setName(name)
                    .setStepCondition(condition)
                    .setType(NSagaCallNodeType.IF);
            currentNodes.add(node);
            return new IfImpl<>(this, node);
        }

        @Override
        public While<While<P>> thenWhile(String name, NSagaCallCondition condition) {
            NSagaCallNodeModel node = new NSagaCallNodeModel()
                    .setId(nextId())
                    .setName(name)
                    .setStepCondition(condition)
                    .setType(NSagaCallNodeType.WHILE);
            currentNodes.add(node);
            return new WhileImpl<>(this, node.getChildren(), node);
        }

        @Override
        public P end() {
            return parent;
        }
    }
}
