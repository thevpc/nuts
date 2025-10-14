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
        this.strategy = factory.createStrategy(model.getStrategy());
        this.factory = factory;
        _updateModel();
    }

    private void _updateModel() {
        workBalancerWorkerLoadMap.clear();
        workBalancerWorkerModelMap.clear();
        if (model.getWorkers() != null) {
            int workerIndex = 0;
            for (NWorkBalancerWorkerModel w : model.getWorkers()) {
                workBalancerWorkerModelMap.put(w.getName(), w);
                workBalancerWorkerLoadMap.put(w.getName(), new NWorkBalancerWorkerLoadImpl(w, workerIndex));
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
    public List<NWorkBalancerRunningJob> getRunningJobs() {
        return Collections.unmodifiableList(new ArrayList<>(runningJobs));
    }

    @Override
    public boolean hasRunningJobs() {
        return !runningJobs.isEmpty();
    }

    @Override
    public int getRunningJobsCount() {
        return runningJobs.size();
    }

    @Override
    public List<NWorkBalancerWorker> getWorkers() {
        if (model.getWorkers() == null) return Collections.emptyList();
        return model.getWorkers().stream().map(NWorkBalancerWorkerImpl::new).collect(Collectors.toList());
    }

    @Override
    public NOptional<NWorkBalancerWorkerLoad> getWorkerLoad(String workerName) {
        NWorkBalancerWorkerLoadImpl worker = workBalancerWorkerLoadMap.get(workerName);
        return NOptional.ofNamed(worker, workerName);
    }

    @Override
    public Map<String, NWorkBalancerWorkerLoad> getWorkerLoads() {
        if (model.getWorkers() == null) return Collections.emptyMap();
        Map<String, NWorkBalancerWorkerLoad> map = new HashMap<>();
        for (NWorkBalancerWorkerModel w : model.getWorkers()) {
            getWorkerLoad(w.getName()).ifPresent(l -> map.put(w.getName(), l));
        }
        return map;
    }

    @Override
    public NOptional<NElement> getOption(String name) {
        if (model.getOptions() != null && model.getOptions().containsKey(name)) {
            return NOptional.of(model.getOptions().get(name));
        }
        return NOptional.ofEmpty();
    }

    @Override
    public Map<String, NElement> getOptions() {
        return model.getOptions() != null ? Collections.unmodifiableMap(model.getOptions()) : Collections.emptyMap();
    }


}
