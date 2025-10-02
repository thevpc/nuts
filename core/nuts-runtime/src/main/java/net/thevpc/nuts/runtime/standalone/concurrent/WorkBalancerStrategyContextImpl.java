package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NOptional;

import java.util.*;
import java.util.stream.Collectors;

public class WorkBalancerStrategyContextImpl implements NWorkBalancerStrategyContext {
    private final NWorkBalancerModel model;

    public WorkBalancerStrategyContextImpl(NWorkBalancerModel model) {
        this.model = Objects.requireNonNull(model);
    }

    @Override
    public List<String> workers() {
        if (model.getWorkers() == null) return Collections.emptyList();
        return model.getWorkers().stream().map(NWorkBalancerWorkerModel::getId).collect(Collectors.toList());
    }

    @Override
    public NOptional<NWorkBalancerWorkerLoad> getWorkerLoad(String workerName) {
        NWorkBalancerWorkerModel worker = findWorker(workerName);
        if (worker == null || worker.getLoadSupplier() == null) {
            return NOptional.ofEmpty();
        }
        return NOptional.of(new NWorkBalancerWorkerLoadImpl(worker));
    }

    @Override
    public Map<String, NWorkBalancerWorkerLoad> getWorkerLoad() {
        if (model.getWorkers() == null) return Collections.emptyMap();
        Map<String, NWorkBalancerWorkerLoad> map = new HashMap<>();
        for (NWorkBalancerWorkerModel w : model.getWorkers()) {
            getWorkerLoad(w.getId()).ifPresent(l -> map.put(w.getId(), l));
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
    public NOptional<NElement> getWorkerOption(String workerName, String name) {
        NWorkBalancerWorkerModel w = findWorker(workerName);
        if(w!=null){
            if (w.getOptions() != null && w.getOptions().containsKey(name)) {
                return NOptional.of(w.getOptions().get(name));
            }
        }
        return NOptional.ofNamedEmpty(name);
    }

    @Override
    public Map<String, NElement> getWorkerOptions(String workerName) {
        NWorkBalancerWorkerModel w = findWorker(workerName);
        if(w!=null){
            if (w.getOptions() != null) {
                return new HashMap<>(w.getOptions());
            }
        }
        return new HashMap<>();
    }

    @Override
    public Map<String, NElement> getOptions() {
        return model.getOptions() != null ? Collections.unmodifiableMap(model.getOptions()) : Collections.emptyMap();
    }

    @Override
    public NOptional<NElement> getWorkerVar(String workerName, String name) {
        Map<String, Map<String, NElement>> v = model.getContext().getVariables();
        Map<String, NElement> workerVars = v.get(workerName);
        return NOptional.ofNamed(workerVars == null ? null : workerVars.get(name), name);
    }

    @Override
    public NOptional<NElement> getVar(String name) {
        if (model.getContext() != null && model.getContext().getVariables() != null) {
            Map<String, NElement> globalVars = model.getContext().getVariables().getOrDefault("", Collections.emptyMap());
            return NOptional.ofNullable(globalVars.get(name));
        }
        return NOptional.ofEmpty();
    }

    @Override
    public NWorkBalancerStrategyContext setWorkerVar(String workerName,String name, NElement value) {
        model.getContext().getVariables().computeIfAbsent(workerName, k -> new HashMap<>()).put(name, value);
        return this;
    }

    @Override
    public NWorkBalancerStrategyContext setVar(String name, NElement value) {
        model.getContext().getVariables().computeIfAbsent("", k -> new HashMap<>()).put(name, value);
        return this;
    }

    public NWorkBalancerWorkerModel findWorker(String name) {
        if (model.getWorkers() != null) {
            for (NWorkBalancerWorkerModel w : model.getWorkers()) {
                if (Objects.equals(w.getId(), name)) return w;
            }
        }
        return null;
    }

}
