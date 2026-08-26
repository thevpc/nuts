package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NOptional;

import java.util.*;
import java.util.stream.Collectors;

public class WorkBalancerStrategyContextImpl implements NWorkBalancerStrategyContext {
    private final NWorkBalancerModel model;
    private final NWorkBalancerImpl<?> workBalancer;

    public WorkBalancerStrategyContextImpl(NWorkBalancerModel model, NWorkBalancerImpl<?> workBalancer) {
        this.model = NAssert.requireNamedNonNull(model);
        this.workBalancer = workBalancer;
    }

    @Override
    public List<NWorkBalancerWorker> workers() {
        if (model.workers() == null) return Collections.emptyList();
        return model.workers().stream().map(NWorkBalancerWorkerImpl::new).collect(Collectors.toList());
    }

    @Override
    public NOptional<NWorkBalancerWorkerLoad> getWorkerLoad(String workerName) {
        return workBalancer.getWorkerLoad(workerName);
    }

    @Override
    public Map<String, NWorkBalancerWorkerLoad> workerLoads() {
        return workBalancer.workerLoads();
    }

    @Override
    public NOptional<NElement> getOption(String name) {
        return workBalancer.getOption(name);
    }

    @Override
    public Map<String, NElement> options() {
        return workBalancer.options();
    }

    @Override
    public NOptional<NElement> getWorkerVar(String workerName, String name) {
        Map<String, Map<String, NElement>> v = model.context().variables();
        Map<String, NElement> workerVars = v.get(workerName);
        return NOptional.ofNamed(workerVars == null ? null : workerVars.get(name), name);
    }

    @Override
    public NOptional<NElement> getSharedVar(String name) {
        if (model.context() != null && model.context().variables() != null) {
            Map<String, NElement> globalVars = model.context().variables().getOrDefault("", Collections.emptyMap());
            return NOptional.ofNullable(globalVars.get(name));
        }
        return NOptional.ofEmpty();
    }

    @Override
    public NWorkBalancerStrategyContext setWorkerVar(String workerName, String name, NElement value) {
        model.context().variables().computeIfAbsent(workerName, k -> new HashMap<>()).put(name, value);
        return this;
    }

    @Override
    public NWorkBalancerStrategyContext setSharedVar(String name, NElement value) {
        model.context().variables().computeIfAbsent("", k -> new HashMap<>()).put(name, value);
        return this;
    }

}
