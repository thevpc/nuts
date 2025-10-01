package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.NExceptions;
import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NScopedValue;
import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.stream.Collectors;

public class NSagaImpl<T> implements NSaga<T> {
    private NSagaModel model;
    private final NSagaStore store;
    private final NBeanContainer beanContainer;
    private NSagaContext fcontext = new NSagaContext() {
        @Override
        public <V> V getVar(String name) {
            return (V) model.getContext().get(name);
        }

        @Override
        public NSagaContext setVar(String name, Object value) {
            model.getContext().put(name, value);
            _store();
            return this;
        }
    };

    public NSagaImpl(NSagaModel model, NSagaStore store, NBeanContainer beanContainer) {
        this.model = model;
        this.store = store;
        this.beanContainer = beanContainer;
        _prepareModel();
        _store(model);
    }

    private void _prepareModel() {
        Set<String> visitedIds = new HashSet<>();
        if (model.getContext().getStatus() == null) {
            model.getContext().setStatus(NSagaExecutionStatus.PENDING);
        }
        model.setId(validateId(model.getId(), visitedIds));
        model.setContext(_prepareNode(model.getContext()));
        model.setNode(_prepareNode(model.getNode(), visitedIds));
        if (model.getContext().getStepsToCompensate() == null) {
            model.getContext().setStepsToCompensate(new ArrayDeque<>());
        }
        for (String s : model.getContext().getStepsToCompensate()) {
            if (!visitedIds.contains(s)) {
                throw new NIllegalArgumentException(NMsg.ofC("invalid id : %s", s));
            }
        }
        for (String s : model.getContext().getStackStepId()) {
            if (!visitedIds.contains(s)) {
                throw new NIllegalArgumentException(NMsg.ofC("invalid id : %s", s));
            }
        }
        if (model.getContext().getStackStepIndex() == null) {
            model.getContext().setStackStepIndex(new ArrayDeque<>());
        }
        if (model.getContext().getStackStepGroup() == null) {
            model.getContext().setStackStepGroup(new ArrayDeque<>());
        }
        if (model.getContext().getStackStepId() == null) {
            model.getContext().setStackStepId(new ArrayDeque<>());
        }
        if (model.getContext().getStackStepIndex().size() != model.getContext().getStackStepGroup().size()) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid stack at : %s", model.getId()));
        }
        if (model.getContext().getStackStepId().size() != model.getContext().getStackStepGroup().size()) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid stack at : %s", model.getId()));
        }
        if (model.getContext().getStackStepId().isEmpty()) {
            if (model.getContext().getStatus() == NSagaExecutionStatus.PENDING) {
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
        NAssert.requireNonNull(m.getType(), "type");
        if (NBlankable.isBlank(m.getId())) {
            m.setId(UUID.randomUUID().toString());
        }
        m.setId(validateId(m.getId(), visitedIds));
        if (m.getCompensationStrategy() == null) {
            m.setCompensationStrategy(NCompensationStrategy.ABORT);
        }
        if (m.getStatus() == null) {
            m.setStatus(NSagaNodeStatus.PENDING);
        }
        switch (m.getType()) {
            case STEP: {
                NAssert.requireNonNull(m.getStepCall(), "call");
                m.setChildren(null);
                m.setStepCondition(null);
                m.setElseIfBranches(null);
                m.setOtherwiseBranch(null);
                break;
            }
            case IF: {
                m.setStepCall(null);
                break;
            }
            case WHILE: {
                m.setStepCall(null);
                m.setElseIfBranches(null);
                m.setOtherwiseBranch(null);
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
    public NSaga<T> reset() {
        NSagaContextModel c = model.getContext();
        _resetContext(c);
        _store();
        return this;
    }

    private void _resetContext(NSagaContextModel c) {
        c.setLastResult(null);
        c.setStartTime(0);
        c.setEndTime(0);
        c.getStackStepId().clear();
        c.getStackStepIndex().clear();
        c.getStackStepGroup().clear();
        c.getValues().clear();
        c.getStepsToCompensate().clear();   // <--- important
        c.setStatus(NSagaExecutionStatus.PENDING);
    }

    @Override
    public NSaga<T> newInstance() {
        NSagaImpl<T> copy = (NSagaImpl<T>) copy();
        copy._resetContext(copy.model.getContext());
        return copy;
    }

    @Override
    public NSagaExecutionStatus status() {
        NSagaExecutionStatus s = model.getContext().getStatus();
        return s == null ? NSagaExecutionStatus.PENDING : s;
    }

    @Override
    public T call() {
        while (true) {
            if (!runStep()) {
                break;
            }
        }
        return getResult();
    }

    @Override
    public NSaga<T> copy() {
        return new NSagaImpl<>(model.clone(), store, beanContainer);
    }

    private void _store(NBooleanRef requireStore) {
        if(requireStore.get()) {
            _store();
            requireStore.set(false);
        }
    }

    private void _store(NSagaModel model) {
        System.out.println(model);
        NScopedValue<NBeanContainer> c = NBeanContainer.current();
        NBeanContainer currContainer = beanContainer == null ? c.get() : beanContainer;
        NRunnable cc = () -> {
            store.save(model);
        };
        if (c == null) {
            cc.run();
        } else {
            c.runWith(currContainer, cc);
        }
    }

    private void _store() {
        _store(model);
    }

    private StackItem _pop() {
        NSagaContextModel ctx = model.getContext();
        if (!ctx.getStackStepId().isEmpty()) {
            String parentId = ctx.getStackStepId().pop();
            int childIndex = ctx.getStackStepIndex().pop();
            String childGroup = ctx.getStackStepGroup().pop();
            return new StackItem(parentId, childGroup, childIndex);
        }
        return null;
    }

    private void _peekUpdateIndex(int index) {
        NSagaContextModel ctx = model.getContext();
        if (!ctx.getStackStepId().isEmpty()) {
            ctx.getStackStepIndex().pop();
            ctx.getStackStepIndex().push(index);
        }
    }

    private StackItem _peek() {
        NSagaContextModel ctx = model.getContext();
        if (!ctx.getStackStepId().isEmpty()) {
            String parentId = ctx.getStackStepId().peek();
            int childIndex = ctx.getStackStepIndex().peek();
            String childGroup = ctx.getStackStepGroup().peek();
            return new StackItem(parentId, childGroup, childIndex);
        }
        return null;
    }

    private void _push(StackItem item) {
        NSagaContextModel ctx = model.getContext();
        ctx.getStackStepId().push(item.id);
        ctx.getStackStepIndex().push(item.index);
        ctx.getStackStepGroup().push(item.group);
    }


    public boolean runStep_children(String nodeId, List<NSagaNodeModel> list, int idx, String group) {
        if (list != null && idx < list.size()) {
            // Push current node back with incremented index
            if (idx + 1 < list.size()) {
                _push(new StackItem(nodeId, group, idx + 1));
            }
            // Push the next child with index 0
            _push(new StackItem(list.get(idx).getId(), group, 0));
            return true;
        }
        return false;
    }

    public boolean runStep() {
        NBooleanRef requireStore = NBooleanRef.of(false);
        if(model.getContext().getStartTime()==0) {
            model.getContext().setStartTime(System.currentTimeMillis());
            _store();
        }
        NSagaContextModel ctx = model.getContext();
        NSagaExecutionStatus status = ctx.getStatus();
        try {
            // If saga is in a compensation-related state, go to compensation handler
            switch (status) {
                case ROLLED_BACK:
                case PARTIAL_ROLLBACK:
                case FAILED:
                    return runCompensationStep(ctx, requireStore);
                case SUCCESS: {
                    if(model.getContext().getEndTime()==0) {
                        model.getContext().setEndTime(System.currentTimeMillis());
                        requireStore.set();
                        _store(requireStore);
                    }
                    return false;
                }
                // otherwise fall through to forward execution (PENDING, RUNNING, etc.)
            }
            StackItem frame = _pop();
            if (frame == null) {
                if (model.getNode() != null) {
                    frame = new StackItem(model.getNode().getId(), null, 0);
                } else {
                    if(model.getContext().getEndTime()==0) {
                        model.getContext().setEndTime(System.currentTimeMillis());
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
                if (runStep_children(node.getId(), node.getChildren(), frame.index, "children")) {

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
        switch (current.getType()) {
            case WHILE:
            case SUITE: {
                current.setStatus(mergeStatus(current.getChildren().stream().map(x -> x.getStatus()).collect(Collectors.toSet())));
                _store(requireStore);
                return true;
            }
            case IF:{
                Set<NSagaNodeStatus> all=new TreeSet<>();
                if(current.getChildren()!=null) {
                    all.addAll(current.getChildren().stream().map(x -> x.getStatus()).collect(Collectors.toSet()));
                }
                if(current.getElseIfBranches()!=null) {
                    all.addAll(current.getElseIfBranches().stream().map(x -> x.getStatus()).collect(Collectors.toSet()));
                }
                if(current.getOtherwiseBranch()!=null) {
                    all.addAll(current.getOtherwiseBranch().stream().map(x -> x.getStatus()).collect(Collectors.toSet()));
                }
                all.remove(NSagaNodeStatus.PENDING);
                current.setStatus(mergeStatus(current.getChildren().stream().map(x -> x.getStatus()).collect(Collectors.toSet())));
                _store(requireStore);
                return true;
            }
        }
        return false;
    }

    private boolean preVisit(StackItem stackItem, NSagaNodeModel current, NBooleanRef requireStore) {
        NSagaContextModel ctx = model.getContext();
        switch (current.getType()) {
            case SUITE: {
                if (!runStep_children(current.getId(), current.getChildren(), stackItem.index, "children")) {
                    postVisit(stackItem, current, requireStore);
                }
                return true;
            }
            case IF: {
                NSagaCondition mainCond = current.getStepCondition();
                try {
                    if (mainCond != null && mainCond.test(fcontext)) {
                        if(!runStep_children(stackItem.id, current.getChildren(), stackItem.index, "children")){
                            postVisit(stackItem, current, requireStore);
                        }
                        return true;
                    }
                } catch (RuntimeException ex) {
                    throw new RuntimeException("Error evaluating IF condition for node " + stackItem.id, ex);
                }

                if (current.getElseIfBranches() != null) {
                    for (NSagaNodeModel elseIfWrapper : current.getElseIfBranches()) {
                        NSagaCondition elseCond = elseIfWrapper.getStepCondition();
                        try {
                            if (elseCond != null && elseCond.test(fcontext)) {
                                // elseIf wrapper's children are the branch
                                if(!runStep_children(stackItem.id, elseIfWrapper.getChildren(), stackItem.index, "elseIf")){
                                    postVisit(stackItem, current, requireStore);
                                }
                                return true;
                            }
                        } catch (RuntimeException ex) {
                            throw new RuntimeException("Error evaluating ELSE-IF condition for node " + elseIfWrapper.getId(), ex);
                        }
                    }
                }
                if(!runStep_children(stackItem.id, current.getOtherwiseBranch(), stackItem.index, "else")){
                    postVisit(stackItem, current, requireStore);
                }
                return true;
            }
            case WHILE:
                NSagaCondition mainCond = current.getStepCondition();
                try {
                    if (mainCond != null && mainCond.test(fcontext)) {
                        runStep_children(stackItem.id, current.getChildren(), stackItem.index, "children");
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
                    current.setStatus(NSagaNodeStatus.RUNNING);
                    _store(requireStore);
                    Object result = current.getStepCall().call(fcontext);
                    model.getContext().setLastResult(result);
                    // mark node as executed if you have such state
                    // Leaf execution. Add to compensation list before executing.
                    if (current.getType() != NSagaNodeType.UNDO) {
                        ctx.getStepsToCompensate().add(current.getId());
                        // optionally set status on node: EXECUTING -> EXECUTED
                    }
                    current.setStatus(NSagaNodeStatus.FINISHED);
                    _store(requireStore);
                    postVisit(stackItem, current, requireStore);
                    // persist the fact that we advanced and recorded last result
                } catch (Exception ex) {
                    if(ctx.getFirstFailStepThrowable()==null){
                        ctx.setFirstFailStepThrowable(ex);
                        ctx.setFirstFailStepId(current.getId());
                        ctx.setFirstFailStepName(current.getName());
                    }
                    // Forward step failed -> set status to FAILED and record throwable.
                    current.setStatus(NSagaNodeStatus.FAILED);
                    ctx.setStatus(NSagaExecutionStatus.FAILED);
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
        if (context.getStepsToCompensate().isEmpty()) {
            // nothing to compensate: finalize state
            context.setStatus(NSagaExecutionStatus.ROLLED_BACK);
            // optionally set end time
            requireStore.set();
            if(model.getContext().getEndTime()==0) {
                model.getContext().setEndTime(System.currentTimeMillis());
                requireStore.set();
            }
            return false;
        }

        String last = null;
        NSagaNodeModel m0 = null;
        try {
            last = context.getStepsToCompensate().removeLast();
            if(last!=null){
                requireStore.set();
                m0 = findById(last);
            }
            if (m0 == null) {
                // missing node — skip it (or mark ignored)
                return true;
            }

            switch (m0.getStatus()) {
                case COMPENSATION_FAILED:
                case COMPENSATION_IGNORED:
                    // already known state: skip
                    break;
                default:
                    m0.setStatus(NSagaNodeStatus.COMPENSATING);
                    _store();
                    // call undo (must be idempotent)
                    m0.getStepCall().undo(fcontext);
                    m0.setStatus(NSagaNodeStatus.COMPENSATED);
                    _store();
                    break;
            }

            // if we've consumed all compensation steps, finish rollback
            if (context.getStepsToCompensate().isEmpty()) {
                context.setStatus(NSagaExecutionStatus.ROLLED_BACK);
                requireStore.set();
                if(model.getContext().getEndTime()==0) {
                    model.getContext().setEndTime(System.currentTimeMillis());
                    requireStore.set();
                }
                return false; // done
            } else {
                // still more to do
                if (context.getStatus() != NSagaExecutionStatus.PARTIAL_ROLLBACK) {
                    context.setStatus(NSagaExecutionStatus.ROLLED_BACK);
                    requireStore.set();
                }
                return true;
            }
        } catch (Exception e) {
            // Compensation for this node failed
            context.setFirstFailStepThrowable(e);
            boolean doAbort = false;
            if (m0 != null) {
                m0.setStatus(NSagaNodeStatus.COMPENSATION_FAILED);
                switch (m0.getCompensationStrategy()) {
                    case ABORT:
                        doAbort = true;
                        for (String s : context.getStepsToCompensate()) {
                            NSagaNodeModel n = findById(s);
                            if (n != null) n.setStatus(NSagaNodeStatus.COMPENSATION_IGNORED);
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
                context.setStatus(NSagaExecutionStatus.FAILED);
                requireStore.set();
                throw NExceptions.ofUncheckedException(e);
            } else {
                // If we continue after a failed compensation, decide the saga status:
                if (context.getStepsToCompensate().isEmpty()) {
                    context.setStatus(NSagaExecutionStatus.FAILED);
                } else {
                    // some compensated, some failed -> partial
                    context.setStatus(NSagaExecutionStatus.PARTIAL_ROLLBACK);
                }
                requireStore.set();
                // continue processing next compensation step on next call
                return true;
            }
        }
    }




    public NSagaNodeModel findById(String id) {
        return findByIdRecursive(model.getNode(), id);
    }

    private NSagaNodeModel findByIdRecursive(NSagaNodeModel node, String id) {
        if (id.equals(node.getId())) {
            return node;
        }
        if (node.getChildren() != null) {
            for (NSagaNodeModel child : node.getChildren()) {
                NSagaNodeModel found = findByIdRecursive(child, id);
                if (found != null) return found;
            }
        }
        if (node.getOtherwiseBranch() != null) {
            for (NSagaNodeModel child : node.getOtherwiseBranch()) {
                NSagaNodeModel found = findByIdRecursive(child, id);
                if (found != null) return found;
            }
        }
        if (node.getElseIfBranches() != null) {
            for (NSagaNodeModel child : node.getElseIfBranches()) {
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
        return (V) model.getContext().getValues().get(key);
    }

    @Override
    public NSaga<T> setVar(String key, Object value) {
        model.getContext().getValues().put(key, value);
        _store();
        return this;
    }

    @Override
    public T getResult() {
        return (T) model.getContext().getLastResult();
    }
}
