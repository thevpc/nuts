package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.time.NDuration;

import java.time.Instant;
import java.util.*;

public class NWorkBalancerCallImpl<T> implements NWorkBalancerCall<T> {

    private final NWorkBalancerStrategy strategy;

    private final List<NWorkBalancerRunningJob<T>> runningJobs = Collections.synchronizedList(new ArrayList<>());
    private final NWorkBalancerCallFactoryImpl factory;
    private final NWorkBalancerModel model;

    public NWorkBalancerCallImpl(NWorkBalancerModel model, NWorkBalancerCallFactoryImpl factory) {
        this.model = model;
        this.strategy = factory.createStrategy(model.getStrategy());
        this.factory = factory;
    }

    @Override
    public T call() {
        NWorkBalancerWorkerModel selectedWorker = selectWorker();

        String jobId = UUID.randomUUID().toString();
        long startNano = System.nanoTime();

        WorkBalancerRunningJob<T> job = new WorkBalancerRunningJob<>(this, jobId, selectedWorker.getId(), startNano);
        runningJobs.add(job);

        try {
            T result = (T) selectedWorker.getCallable().call();
            job.setEndNano(System.nanoTime());
            return result;
        } finally {
            runningJobs.remove(job);
        }
    }

    private NWorkBalancerWorkerModel selectWorker() {
        WorkBalancerStrategyContextImpl w = new WorkBalancerStrategyContextImpl(model);
        String s = strategy.selectWorker(w);
        return w.findWorker(s);
    }

    private double resolveWorkerLoad(NWorkBalancerWorkerModel w) {
        NWorkBalancerHostLoadProvider provider = w.getLoadSupplier();
        if (provider != null) {
            return provider.resolveHostLoad();
        }
        return 0.0;
    }

    @Override
    public List<NWorkBalancerRunningJob<T>> runningJobs() {
        return Collections.unmodifiableList(new ArrayList<>(runningJobs));
    }

    @Override
    public boolean hasRunningJobs() {
        return !runningJobs.isEmpty();
    }

    @Override
    public int runningJobsCount() {
        return runningJobs.size();
    }

    // --- Inner class for running job ---
    private static class WorkBalancerRunningJob<T> implements NWorkBalancerRunningJob<T> {
        private final NWorkBalancerCall<T> call;
        private final String jobId;
        private final String workerName;
        private final long startNano;
        private long endNano;

        public WorkBalancerRunningJob(NWorkBalancerCall<T> call, String jobId, String workerName, long startNano) {
            this.call = call;
            this.jobId = jobId;
            this.workerName = workerName;
            this.startNano = startNano;
        }

        @Override
        public NWorkBalancerCall<T> callable() {
            return call;
        }

        @Override
        public String jobId() {
            return jobId;
        }

        @Override
        public Instant startTime() {
            return Instant.ofEpochMilli(startNano / 1_000_000);
        }

        @Override
        public NDuration duration() {
            long now = (endNano > 0 ? endNano : System.nanoTime()) - startNano;
            return NDuration.ofNanos(now);
        }

        @Override
        public String workerName() {
            return workerName;
        }

        public void setEndNano(long endNano) {
            this.endNano = endNano;
        }
    }
}
