package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.NIllegalStateException;
import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NMsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NWorkBalancerCallBuilderImpl<T> implements NWorkBalancerCallBuilder<T> {
    private final String id;
    private final List<NWorkBalancerWorkerModel> workers = new ArrayList<>();
    private String strategy;
    private final Map<String, NElement> options = new HashMap<>();
    private final NWorkBalancerCallFactoryImpl factory;

    public NWorkBalancerCallBuilderImpl(String id, NWorkBalancerCallFactoryImpl factory) {
        this.id = id;
        this.factory = factory;
    }

    @Override
    public WorkerBuilder<T> addWorker(String workerName, NCallable<T> callable) {
        NWorkBalancerWorkerModel worker = new NWorkBalancerWorkerModel()
                .setId(workerName)
                .setCallable(callable);
        workers.add(worker);
        return new WorkerBuilderImpl<>(this, worker);
    }

    @Override
    public NWorkBalancerCallBuilder<T> remove(String workerName) {
        workers.removeIf(w -> workerName.equals(w.getId()));
        return this;
    }

    @Override
    public NWorkBalancerCallBuilder<T> setStrategy(String strategy) {
        this.strategy = strategy;
        return this;
    }

    @Override
    public NWorkBalancerCallBuilder<T> setStrategy(NWorkBalancerDefaultStrategy strategy) {
        this.strategy = strategy == null ? null : strategy.id();
        return this;
    }

    @Override
    public NWorkBalancerCallBuilder<T> setOption(String optionName, NElement optionValue) {
        options.put(optionName, optionValue);
        return this;
    }

    @Override
    public NWorkBalancerCall<T> build() {
        if (workers.isEmpty()) {
            throw new NIllegalStateException(NMsg.ofC("No workers defined"));
        }
        NWorkBalancerModel model = new NWorkBalancerModel();
        model.setId(id);
        model.setWorkers(new ArrayList<>(workers).stream().map(x -> x.copy()).collect(Collectors.toList()));
        model.setStrategy(strategy);
        model.setOptions(new HashMap<>(options));
        return new NWorkBalancerCallImpl<>(model, factory);
    }

    // --- WorkerBuilder implementation ---
    private static class WorkerBuilderImpl<T> implements WorkerBuilder<T> {
        private final NWorkBalancerCallBuilderImpl<T> parent;
        private final NWorkBalancerWorkerModel worker;

        WorkerBuilderImpl(NWorkBalancerCallBuilderImpl<T> parent, NWorkBalancerWorkerModel worker) {
            this.parent = parent;
            this.worker = worker;
        }

        @Override
        public NWorkBalancerCallBuilder<T> setOption(String workerName, String optionName, NElement optionValue) {
            if (workerName.equals(worker.getId())) {
                if (worker.getOptions() == null) {
                    worker.setOptions(new HashMap<>());
                }
                worker.getOptions().put(optionName, optionValue);
            }
            return parent;
        }

        @Override
        public NWorkBalancerCallBuilder<T> setHostLoadProvider(String workerName, NWorkBalancerHostLoadProvider hostLoadProvider) {
            if (workerName.equals(worker.getId())) {
                worker.setLoadSupplier(hostLoadProvider);
            }
            return parent;
        }

        @Override
        public NWorkBalancerCallBuilder<T> then() {
            return parent;
        }
    }
}
