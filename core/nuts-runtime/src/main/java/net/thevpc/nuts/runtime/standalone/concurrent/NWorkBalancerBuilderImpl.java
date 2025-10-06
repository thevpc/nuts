package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.util.NIllegalStateException;
import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NMsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NWorkBalancerBuilderImpl<T> implements NWorkBalancerBuilder<T> {
    private final String id;
    private final List<NWorkBalancerWorkerModel> workers = new ArrayList<>();
    private String strategy;
    private final Map<String, NElement> options = new HashMap<>();
    private final NWorkBalancerFactoryImpl factory;

    public NWorkBalancerBuilderImpl(String id, NWorkBalancerFactoryImpl factory) {
        this.id = id;
        this.factory = factory;
    }

    @Override
    public WorkerBuilder<T> addWorker(String workerName) {
        NWorkBalancerWorkerModel worker = new NWorkBalancerWorkerModel()
                .setName(workerName)
                .setWeight(1)
                ;
        workers.add(worker);
        return new WorkerBuilderImpl<>(this, worker);
    }

    @Override
    public NWorkBalancerBuilder<T> remove(String workerName) {
        workers.removeIf(w -> workerName.equals(w.getName()));
        return this;
    }

    @Override
    public NWorkBalancerBuilder<T> setStrategy(String strategy) {
        this.strategy = strategy;
        return this;
    }

    @Override
    public NWorkBalancerBuilder<T> setStrategy(NWorkBalancerDefaultStrategy strategy) {
        this.strategy = strategy == null ? null : strategy.id();
        return this;
    }

    @Override
    public NWorkBalancerBuilder<T> setOption(String optionName, NElement optionValue) {
        options.put(optionName, optionValue);
        return this;
    }

    @Override
    public NWorkBalancer<T> build() {
        if (workers.isEmpty()) {
            throw new NIllegalStateException(NMsg.ofC("No workers defined"));
        }
        NWorkBalancerModel model = new NWorkBalancerModel();
        model.setId(id);
        model.setWorkers(new ArrayList<>(workers).stream().map(x -> x.copy()).collect(Collectors.toList()));
        model.setStrategy(strategy);
        model.setOptions(new HashMap<>(options));
        return new NWorkBalancerImpl<>(model, factory);
    }

    // --- WorkerBuilder implementation ---
    private static class WorkerBuilderImpl<T> implements WorkerBuilder<T> {
        private final NWorkBalancerBuilderImpl<T> parent;
        private final NWorkBalancerWorkerModel worker;

        WorkerBuilderImpl(NWorkBalancerBuilderImpl<T> parent, NWorkBalancerWorkerModel worker) {
            this.parent = parent;
            this.worker = worker;
        }

        @Override
        public WorkerBuilder<T> withWeight(float weight) {
            worker.setWeight(weight);
            return this;
        }

        @Override
        public WorkerBuilder<T> withOption(String optionName, NElement optionValue) {
            if (worker.getOptions() == null) {
                worker.setOptions(new HashMap<>());
            }
            worker.getOptions().put(optionName, optionValue);
            return this;
        }

        @Override
        public WorkerBuilder<T> withHostLoadMetricsProvider(NWorkBalancerHostLoadMetricProvider hostLoadMetricsProvider) {
            worker.setHostLoadMetricsProvider(hostLoadMetricsProvider);
            return this;
        }

        @Override
        public NWorkBalancerBuilder<T> then() {
            return parent;
        }
    }
}
