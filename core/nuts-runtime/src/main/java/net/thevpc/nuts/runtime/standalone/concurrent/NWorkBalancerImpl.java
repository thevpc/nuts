package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.util.NOptional;

import java.util.*;
import java.util.stream.Collectors;

public class NWorkBalancerImpl<T> implements NWorkBalancer<T> {

    final NWorkBalancerStrategy strategy;

    final List<NWorkBalancerRunningJob> runningJobs = Collections.synchronizedList(new ArrayList<>());
    private final NWorkBalancerFactoryImpl factory;
    final NWorkBalancerModel model;
    private final Map<String, NWorkBalancerWorkerLoadImpl> workBalancerWorkerLoadMap = new HashMap<>();
    private final Map<String, NWorkBalancerWorkerModel> workBalancerWorkerModelMap = new HashMap<>();

    public NWorkBalancerImpl(NWorkBalancerModel model, NWorkBalancerFactoryImpl factory) {
        this.model = model;
        this.strategy = factory.createStrategy(model.strategy());
        this.factory = factory;
        _updateModel();
    }

    private void _updateModel() {
        workBalancerWorkerLoadMap.clear();
        workBalancerWorkerModelMap.clear();
        if (model.workers() != null) {
            int workerIndex = 0;
            for (NWorkBalancerWorkerModel w : model.workers()) {
                workBalancerWorkerModelMap.put(w.name(), w);
                workBalancerWorkerLoadMap.put(w.name(), new NWorkBalancerWorkerLoadImpl(w, workerIndex));
                workerIndex++;
            }
        }
    }

    @Override
    public NCallable<T> of(String name, NWorkBalancerJob<T> job) {
        return new NCallableFromJob(this, name, job);
    }

    NWorkBalancerWorkerLoadImpl selectWorker() {
        WorkBalancerStrategyContextImpl w = new WorkBalancerStrategyContextImpl(model, this);
        String s = strategy.selectWorker(w);
        return NOptional.ofNamed(workBalancerWorkerLoadMap.get(s), s).get();
    }

    @Override
    public List<NWorkBalancerRunningJob> runningJobs() {
        return Collections.unmodifiableList(new ArrayList<>(runningJobs));
    }

    @Override
    public boolean isRunning() {
        return !runningJobs.isEmpty();
    }

    @Override
    public int runningJobsCount() {
        return runningJobs.size();
    }

    @Override
    public List<NWorkBalancerWorker> workers() {
        if (model.workers() == null) return Collections.emptyList();
        return model.workers().stream().map(NWorkBalancerWorkerImpl::new).collect(Collectors.toList());
    }

    @Override
    public NOptional<NWorkBalancerWorkerLoad> getWorkerLoad(String workerName) {
        NWorkBalancerWorkerLoadImpl worker = workBalancerWorkerLoadMap.get(workerName);
        return NOptional.ofNamed(worker, workerName);
    }

    @Override
    public Map<String, NWorkBalancerWorkerLoad> workerLoads() {
        if (model.workers() == null) return Collections.emptyMap();
        Map<String, NWorkBalancerWorkerLoad> map = new HashMap<>();
        for (NWorkBalancerWorkerModel w : model.workers()) {
            getWorkerLoad(w.name()).ifPresent(l -> map.put(w.name(), l));
        }
        return map;
    }

    @Override
    public NOptional<NElement> getOption(String name) {
        if (model.options() != null && model.options().containsKey(name)) {
            return NOptional.of(model.options().get(name));
        }
        return NOptional.ofEmpty();
    }

    @Override
    public Map<String, NElement> options() {
        return model.options() != null ? Collections.unmodifiableMap(model.options()) : Collections.emptyMap();
    }


}
