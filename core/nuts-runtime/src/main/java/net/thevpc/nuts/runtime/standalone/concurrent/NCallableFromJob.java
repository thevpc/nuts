package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NWorkBalancerJob;
import net.thevpc.nuts.concurrent.NWorkBalancerStrategyEvent;
import net.thevpc.nuts.concurrent.NCallable;

import java.util.UUID;

class NCallableFromJob<T> implements NCallable<T> {
    private final NWorkBalancerImpl nWorkBalancer;
    private final NWorkBalancerJob<T> job;
    private final String jobName;

    public NCallableFromJob(NWorkBalancerImpl nWorkBalancer, String jobName, NWorkBalancerJob<T> job) {
        this.nWorkBalancer = nWorkBalancer;
        this.job = job;
        this.jobName = jobName;
    }

    @Override
    public T call() {
        NWorkBalancerWorkerLoadImpl selectedWorker = nWorkBalancer.selectWorker();
        String jobId = UUID.randomUUID().toString();
        long startNano = System.nanoTime();
        selectedWorker.totalJobsCount.incrementAndGet();
        selectedWorker.activeJobsCount.incrementAndGet();
        WorkBalancerRunningJob<T> runningJob = new WorkBalancerRunningJob<>(job, jobId, jobName, selectedWorker.worker.getName(), startNano);
        nWorkBalancer.runningJobs.add(runningJob);
        selectedWorker.runningJobs.add(runningJob);
        nWorkBalancer.strategy.onStartCall(new NWorkBalancerStrategyEvent(jobId, jobName, selectedWorker.worker.getName(), startNano, 0, null));
        RuntimeException throwable = null;
        try {
            T result = (T) job.call(new NWorkBalancerJobContextImpl(jobId, jobName, new NWorkBalancerWorkerImpl(selectedWorker.worker),selectedWorker.getWorkerIndex(), nWorkBalancer.model));
            selectedWorker.succeededJobCount.incrementAndGet();
            return result;
        } catch (RuntimeException ex) {
            throwable = ex;
            throw ex;
        } finally {
            long endNano = System.nanoTime();
            runningJob.setEndNano(endNano);
            runningJob.setThrowable(throwable);
            nWorkBalancer.runningJobs.remove(runningJob);
            selectedWorker.runningJobs.remove(runningJob);
            if(throwable!=null){
                selectedWorker.failedJobsCount.incrementAndGet();
            }
            selectedWorker.completedJobsTotalDurationNano.addAndGet(endNano - startNano);
            selectedWorker.activeJobsCount.decrementAndGet();
            nWorkBalancer.strategy.onEndCall(new NWorkBalancerStrategyEvent(jobId, jobName, selectedWorker.worker.getName(), startNano, endNano, throwable));
        }
    }
}
