package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NExceptions;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.stream.Collectors;

public class NSagaCallableImpl<T> implements NSagaCallable<T> {
    private NSagaModel model;
    private final NSagaStore store;
    private NSagaContext fcontext = new NSagaContext() {
        @Override
        public <V> V getVar(String name) {
            return (V) model.context().get(name);
        }

        @Override
        public NSagaContext setVar(String name, Object value) {
            model.context().put(name, value);
            _store();
            return this;
        }
    };

    public NSagaCallableImpl(NSagaModel model, NSagaStore store) {
        this.model = model;
        this.store = store;
        _prepareModel();
        _store(model);
    }

    private void _prepareModel() {
        Set<String> visitedIds = new HashSet<>();
        if (model.context().status() == null) {
            model.context().status(NSagaStatus.PENDING);
        }
        model.id(validateId(model.id(), visitedIds));
        model.context(_prepareNode(model.context()));
        model.node(_prepareNode(model.node(), visitedIds));
        if (model.context().stepsToCompensate() == null) {
            model.context().stepsToCompensate(new ArrayDeque<>());
        }
        for (String s : model.context().stepsToCompensate()) {
            if (!visitedIds.contains(s)) {
                throw new NIllegalArgumentException(NMsg.ofC("invalid id : %s", s));
            }
        }
        for (String s : model.context().stackStepId()) {
            if (!visitedIds.contains(s)) {
                throw new NIllegalArgumentException(NMsg.ofC("invalid id : %s", s));
            }
        }
        if (model.context().stackStepIndex() == null) {
            model.context().stackStepIndex(new ArrayDeque<>());
        }
        if (model.context().stackStepGroup() == null) {
            model.context().stackStepGroup(new ArrayDeque<>());
        }
        if (model.context().stackStepId() == null) {
            model.context().stackStepId(new ArrayDeque<>());
        }
        if (model.context().stackStepIndex().size() != model.context().stackStepGroup().size()) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid stack at : %s", model.id()));
        }
        if (model.context().stackStepId().size() != model.context().stackStepGroup().size()) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid stack at : %s", model.id()));
        }
        if (model.context().stackStepId().isEmpty()) {
            if (model.context().status() == NSagaStatus.PENDING) {
//                if (id == null) {
//                    return null;
//                }
//                for (NSagaNodeModel node : model.getNodes()) {
//                    NSagaNodeModel found = findByIdRecursive(node, id);
//                    if (found != null) return found;
//                }
//                return null;
            }
        }
    }

//    private StackItem findStartStackItem() {
//        for (NSagaNodeModel node : model.getNodes()) {
//            StackItem found = findNextStackItem(null,node);
//            if (found != null) {
//                return found;
//            }
//        }
//        return null;
//    }
//
//    private StackItem findNextStackItem(StackItem start,NSagaNodeModel from) {
//        if(start == null){
//            return new StackItem(from.getId(),null,0);
//        }
//        switch (from.getType()) {
//            case STEP:{
//                return null;
//            }
//            case WHILE:
//            case SUITE:{
//                if(start.group==null) {
//                    return new StackItem(from.getId(), "children", 0);
//                }else{
//                    if(start.index+1<findById(start.id).getChildren().size()){
//                        return new StackItem(from.getId(),"children",start.index+1);
//                    }
//                    return null;
//                }
//            }
//            case IF:{
//                throw new IllegalArgumentException("unsupported");
//            }
//        }
//        switch (from.getStatus()) {
//            ca
//        }
//    }


    private static class StackItem {
        String id;
        String group;
        int index;

        public StackItem(String id, String group, int index) {
            this.id = id;
            this.group = group;
            this.index = index;
        }
    }

    private NSagaNodeModel _prepareNode(NSagaNodeModel m, Set<String> visitedIds) {
        if (m == null) {
            return null;
        }
        NAssert.requireNamedNonNull(m.type(), "type");
        if (NBlankable.isBlank(m.id())) {
            m.id(UUID.randomUUID().toString());
        }
        m.id(validateId(m.id(), visitedIds));
        if (m.compensationStrategy() == null) {
            m.compensationStrategy(NCompensationStrategy.ABORT);
        }
        if (m.status() == null) {
            m.status(NSagaNodeStatus.PENDING);
        }
        switch (m.type()) {
            case STEP: {
                NAssert.requireNamedNonNull(m.stepCall(), "call");
                m.children(null);
                m.stepCondition(null);
                m.elseIfBranches(null);
                m.otherwiseBranch(null);
                break;
            }
            case IF: {
                m.stepCall(null);
                break;
            }
            case WHILE: {
                m.stepCall(null);
                m.elseIfBranches(null);
                m.otherwiseBranch(null);
                break;
            }
        }
        return m;
    }

    private NSagaContextModel _prepareNode(NSagaContextModel m) {
        if (m == null) {
            m = new NSagaContextModel();
        }
        return m;
    }

    private String validateId(String id, Set<String> visitedIds) {
        if (NBlankable.isBlank(id)) {
            return UUID.randomUUID().toString();
        } else {
            if (visitedIds.add(id)) {
                return id;
            } else {
                throw new NIllegalArgumentException(NMsg.ofC("duplicate id  : %s", id));
            }
        }
    }

    @Override
    public NSagaCallable<T> reset() {
        NSagaContextModel c = model.context();
        _resetContext(c);
        _store();
        return this;
    }

    private void _resetContext(NSagaContextModel c) {
        c.lastResult(null);
        c.startTime(0);
        c.endTime(0);
        c.stackStepId().clear();
        c.stackStepIndex().clear();
        c.stackStepGroup().clear();
        c.values().clear();
        c.stepsToCompensate().clear();   // <--- important
        c.status(NSagaStatus.PENDING);
    }

    @Override
    public NSagaCallable<T> newInstance() {
        NSagaCallableImpl<T> copy = (NSagaCallableImpl<T>) copy();
        copy._resetContext(copy.model.context());
        return copy;
    }

    @Override
    public NSagaStatus status() {
        NSagaStatus s = model.context().status();
        return s == null ? NSagaStatus.PENDING : s;
    }

    @Override
    public T call() {
        while (true) {
            if (!runStep()) {
                break;
            }
        }
        return result();
    }

    @Override
    public NSagaCallable<T> copy() {
        return new NSagaCallableImpl<>(model.clone(), store);
    }

    private void _store(NBooleanRef requireStore) {
        if(requireStore.get()) {
            _store();
            requireStore.set(false);
        }
    }

    private void _store(NSagaModel model) {
        NRunnable cc = () -> {
            store.save(model);
        };
        NBeanContainer.scopedStack().runWith(NBeanContainer.current(), cc);
    }

    private void _store() {
        _store(model);
    }

    private StackItem _pop() {
        NSagaContextModel ctx = model.context();
        if (!ctx.stackStepId().isEmpty()) {
            String parentId = ctx.stackStepId().pop();
            int childIndex = ctx.stackStepIndex().pop();
            String childGroup = ctx.stackStepGroup().pop();
            return new StackItem(parentId, childGroup, childIndex);
        }
        return null;
    }

    private void _peekUpdateIndex(int index) {
        NSagaContextModel ctx = model.context();
        if (!ctx.stackStepId().isEmpty()) {
            ctx.stackStepIndex().pop();
            ctx.stackStepIndex().push(index);
        }
    }

    private StackItem _peek() {
        NSagaContextModel ctx = model.context();
        if (!ctx.stackStepId().isEmpty()) {
            String parentId = ctx.stackStepId().peek();
            int childIndex = ctx.stackStepIndex().peek();
            String childGroup = ctx.stackStepGroup().peek();
            return new StackItem(parentId, childGroup, childIndex);
        }
        return null;
    }

    private void _push(StackItem item) {
        NSagaContextModel ctx = model.context();
        ctx.stackStepId().push(item.id);
        ctx.stackStepIndex().push(item.index);
        ctx.stackStepGroup().push(item.group);
    }


    public boolean runStep_children(String nodeId, List<NSagaNodeModel> list, int idx, String group) {
        if (list != null && idx < list.size()) {
            // Push current node back with incremented index
            if (idx + 1 < list.size()) {
                _push(new StackItem(nodeId, group, idx + 1));
            }
            // Push the next child with index 0
            _push(new StackItem(list.get(idx).id(), group, 0));
            return true;
        }
        return false;
    }

    public boolean runStep() {
        NBooleanRef requireStore = NBooleanRef.of(false);
        if(model.context().startTime()==0) {
            model.context().startTime(System.currentTimeMillis());
            _store();
        }
        NSagaContextModel ctx = model.context();
        NSagaStatus status = ctx.status();
        try {
            // If saga is in a compensation-related state, go to compensation handler
            switch (status) {
                case ROLLED_BACK:
                case PARTIAL_ROLLBACK:
                case FAILED:
                    return runCompensationStep(ctx, requireStore);
                case SUCCESS: {
                    if(model.context().endTime()==0) {
                        model.context().endTime(System.currentTimeMillis());
                        requireStore.set();
                        _store(requireStore);
                    }
                    return false;
                }
                // otherwise fall through to forward execution (PENDING, RUNNING, etc.)
            }
            StackItem frame = _pop();
            if (frame == null) {
                if (model.node() != null) {
                    frame = new StackItem(model.node().id(), null, 0);
                } else {
                    if(model.context().endTime()==0) {
                        model.context().endTime(System.currentTimeMillis());
                        requireStore.set();
                        _store(requireStore);
                    }
                    return false;
                }
            }
            requireStore.set();
            NSagaNodeModel node = findById(frame.id);
            if (frame.index == 0) {
                // First time we see this node
                preVisit(frame, node, requireStore);
                //runStep_children(node.getId(), node.getChildren(), frame.index, "children");
            } else {
                if (runStep_children(node.id(), node.children(), frame.index, "children")) {

                } else {
                    postVisit(frame, node, requireStore);
                }
            }
            return true;
        } finally {
            _store(requireStore);
        }
    }

    private NSagaNodeStatus mergeStatus(Set<NSagaNodeStatus> collect){
        if(collect.isEmpty()) {
            return (NSagaNodeStatus.FINISHED);
        }else if(collect.size() == 1) {
            return (collect.stream().findFirst().get());
        }else{
            if(collect.contains(NSagaNodeStatus.COMPENSATION_FAILED)) {
                return (NSagaNodeStatus.COMPENSATION_FAILED);
            }else if(collect.contains(NSagaNodeStatus.COMPENSATION_IGNORED)) {
                return (NSagaNodeStatus.COMPENSATION_IGNORED);
            }else if(collect.contains(NSagaNodeStatus.FAILED)) {
                return (NSagaNodeStatus.FAILED);
            }else if(collect.contains(NSagaNodeStatus.COMPENSATING)) {
                return (NSagaNodeStatus.COMPENSATING);
            }else if(collect.contains(NSagaNodeStatus.COMPENSATED)) {
                return (NSagaNodeStatus.COMPENSATED);
            }else if(collect.contains(NSagaNodeStatus.RUNNING)) {
                return (NSagaNodeStatus.RUNNING);
            }else if(collect.contains(NSagaNodeStatus.IGNORED)) {
                return (NSagaNodeStatus.IGNORED);
            }else if(collect.contains(NSagaNodeStatus.FINISHED)) {
                return (NSagaNodeStatus.FINISHED);
            }else if(collect.contains(NSagaNodeStatus.PENDING)) {
                return (NSagaNodeStatus.PENDING);
            }
        }
        return (NSagaNodeStatus.PENDING);
    }


    private boolean postVisit(StackItem stackItem, NSagaNodeModel current, NBooleanRef requireStore) {
        switch (current.type()) {
            case WHILE:
            case SUITE: {
                current.status(mergeStatus(current.children().stream().map(x -> x.status()).collect(Collectors.toSet())));
                _store(requireStore);
                return true;
            }
            case IF:{
                Set<NSagaNodeStatus> all=new TreeSet<>();
                if(current.children()!=null) {
                    all.addAll(current.children().stream().map(x -> x.status()).collect(Collectors.toSet()));
                }
                if(current.elseIfBranches()!=null) {
                    all.addAll(current.elseIfBranches().stream().map(x -> x.status()).collect(Collectors.toSet()));
                }
                if(current.otherwiseBranch()!=null) {
                    all.addAll(current.otherwiseBranch().stream().map(x -> x.status()).collect(Collectors.toSet()));
                }
                all.remove(NSagaNodeStatus.PENDING);
                current.status(mergeStatus(current.children().stream().map(x -> x.status()).collect(Collectors.toSet())));
                _store(requireStore);
                return true;
            }
        }
        return false;
    }

    private boolean preVisit(StackItem stackItem, NSagaNodeModel current, NBooleanRef requireStore) {
        NSagaContextModel ctx = model.context();
        switch (current.type()) {
            case SUITE: {
                if (!runStep_children(current.id(), current.children(), stackItem.index, "children")) {
                    postVisit(stackItem, current, requireStore);
                }
                return true;
            }
            case IF: {
                NSagaCondition mainCond = current.stepCondition();
                try {
                    if (mainCond != null && mainCond.test(fcontext)) {
                        if(!runStep_children(stackItem.id, current.children(), stackItem.index, "children")){
                            postVisit(stackItem, current, requireStore);
                        }
                        return true;
                    }
                } catch (RuntimeException ex) {
                    throw new RuntimeException("Error evaluating IF condition for node " + stackItem.id, ex);
                }

                if (current.elseIfBranches() != null) {
                    for (NSagaNodeModel elseIfWrapper : current.elseIfBranches()) {
                        NSagaCondition elseCond = elseIfWrapper.stepCondition();
                        try {
                            if (elseCond != null && elseCond.test(fcontext)) {
                                // elseIf wrapper's children are the branch
                                if(!runStep_children(stackItem.id, elseIfWrapper.children(), stackItem.index, "elseIf")){
                                    postVisit(stackItem, current, requireStore);
                                }
                                return true;
                            }
                        } catch (RuntimeException ex) {
                            throw new RuntimeException("Error evaluating ELSE-IF condition for node " + elseIfWrapper.id(), ex);
                        }
                    }
                }
                if(!runStep_children(stackItem.id, current.otherwiseBranch(), stackItem.index, "else")){
                    postVisit(stackItem, current, requireStore);
                }
                return true;
            }
            case WHILE:
                NSagaCondition mainCond = current.stepCondition();
                try {
                    if (mainCond != null && mainCond.test(fcontext)) {
                        runStep_children(stackItem.id, current.children(), stackItem.index, "children");
                        //repeat self!
                        _push(new StackItem(stackItem.id, null, 0));
                        return true;
                    }
                    postVisit(stackItem, current, requireStore);
                } catch (RuntimeException ex) {
                    throw new RuntimeException("Error evaluating IF condition for node " + stackItem.id, ex);
                }
            default: {
                try {
                    current.status(NSagaNodeStatus.RUNNING);
                    _store(requireStore);
                    Object result = current.stepCall().call(fcontext);
                    model.context().lastResult(result);
                    // mark node as executed if you have such state
                    // Leaf execution. Add to compensation list before executing.
                    if (current.type() != NSagaNodeType.UNDO) {
                        ctx.stepsToCompensate().add(current.id());
                        // optionally set status on node: EXECUTING -> EXECUTED
                    }
                    current.status(NSagaNodeStatus.FINISHED);
                    _store(requireStore);
                    postVisit(stackItem, current, requireStore);
                    // persist the fact that we advanced and recorded last result
                } catch (Exception ex) {
                    if(ctx.firstFailStepError()==null){
                        ctx.firstFailStepError(ex);
                        ctx.firstFailStepId(current.id());
                        ctx.firstFailStepName(current.name());
                    }
                    // Forward step failed -> set status to FAILED and record throwable.
                    current.status(NSagaNodeStatus.FAILED);
                    ctx.status(NSagaStatus.FAILED);
                    _store(requireStore);

                    // Immediately start compensation now (don't rethrow)
                    // Option A: return true so caller loop calls runStep() again and sees FAILED
                    // Option B: directly invoke runCompensationStep to begin undoing now.
                    // I prefer direct invocation so behavior is immediate and deterministic.
                    return runCompensationStep(ctx, requireStore);
                }
                return true;
            }
        }
    }

    private boolean runCompensationStep(NSagaContextModel context, NBooleanRef requireStore) {
        if (context.stepsToCompensate().isEmpty()) {
            // nothing to compensate: finalize state
            context.status(NSagaStatus.ROLLED_BACK);
            // optionally set end time
            requireStore.set();
            if(model.context().endTime()==0) {
                model.context().endTime(System.currentTimeMillis());
                requireStore.set();
            }
            return false;
        }

        String last = null;
        NSagaNodeModel m0 = null;
        try {
            last = context.stepsToCompensate().removeLast();
            if(last!=null){
                requireStore.set();
                m0 = findById(last);
            }
            if (m0 == null) {
                // missing node — skip it (or mark ignored)
                return true;
            }

            switch (m0.status()) {
                case COMPENSATION_FAILED:
                case COMPENSATION_IGNORED:
                    // already known state: skip
                    break;
                default:
                    m0.status(NSagaNodeStatus.COMPENSATING);
                    _store();
                    // call undo (must be idempotent)
                    m0.stepCall().undo(fcontext);
                    m0.status(NSagaNodeStatus.COMPENSATED);
                    _store();
                    break;
            }

            // if we've consumed all compensation steps, finish rollback
            if (context.stepsToCompensate().isEmpty()) {
                context.status(NSagaStatus.ROLLED_BACK);
                requireStore.set();
                if(model.context().endTime()==0) {
                    model.context().endTime(System.currentTimeMillis());
                    requireStore.set();
                }
                return false; // done
            } else {
                // still more to do
                if (context.status() != NSagaStatus.PARTIAL_ROLLBACK) {
                    context.status(NSagaStatus.ROLLED_BACK);
                    requireStore.set();
                }
                return true;
            }
        } catch (Exception e) {
            // Compensation for this node failed
            context.firstFailStepError(e);
            boolean doAbort = false;
            if (m0 != null) {
                m0.status(NSagaNodeStatus.COMPENSATION_FAILED);
                switch (m0.compensationStrategy()) {
                    case ABORT:
                        doAbort = true;
                        for (String s : context.stepsToCompensate()) {
                            NSagaNodeModel n = findById(s);
                            if (n != null) n.status(NSagaNodeStatus.COMPENSATION_IGNORED);
                        }
                        break;
                    case IGNORE:
                        // mark failed but continue
                        break;
                }
                requireStore.set();
            }

            // If abort policy, rethrow to allow caller to deal with the catastrophic failure
            if (doAbort) {
                context.status(NSagaStatus.FAILED);
                requireStore.set();
                throw NExceptions.ofUncheckedException(e);
            } else {
                // If we continue after a failed compensation, decide the saga status:
                if (context.stepsToCompensate().isEmpty()) {
                    context.status(NSagaStatus.FAILED);
                } else {
                    // some compensated, some failed -> partial
                    context.status(NSagaStatus.PARTIAL_ROLLBACK);
                }
                requireStore.set();
                // continue processing next compensation step on next call
                return true;
            }
        }
    }




    public NSagaNodeModel findById(String id) {
        return findByIdRecursive(model.node(), id);
    }

    private NSagaNodeModel findByIdRecursive(NSagaNodeModel node, String id) {
        if (id.equals(node.id())) {
            return node;
        }
        if (node.children() != null) {
            for (NSagaNodeModel child : node.children()) {
                NSagaNodeModel found = findByIdRecursive(child, id);
                if (found != null) return found;
            }
        }
        if (node.otherwiseBranch() != null) {
            for (NSagaNodeModel child : node.otherwiseBranch()) {
                NSagaNodeModel found = findByIdRecursive(child, id);
                if (found != null) return found;
            }
        }
        if (node.elseIfBranches() != null) {
            for (NSagaNodeModel child : node.elseIfBranches()) {
                NSagaNodeModel found = findByIdRecursive(child, id);
                if (found != null) return found;
            }
        }
        return null;
    }

    @Override
    public T callOrElse(NCallable<T> other) {
        try {
            return call();
        } catch (Exception ex) {
            return other.call();
        }
    }

    @Override
    public <V> V getVar(String key) {
        return (V) model.context().values().get(key);
    }

    @Override
    public NSagaCallable<T> setVar(String key, Object value) {
        model.context().values().put(key, value);
        _store();
        return this;
    }

    @Override
    public T result() {
        return (T) model.context().lastResult();
    }
}
