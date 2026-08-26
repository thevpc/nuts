package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.time.NDuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

class NWorkBalancerWorkerLoadImpl implements NWorkBalancerWorkerLoad {
    final NWorkBalancerWorkerModel worker;
    final int workerIndex;

    public AtomicLong activeJobsCount = new AtomicLong(0);

    public AtomicLong totalJobsCount = new AtomicLong(0);

    public AtomicLong succeededJobCount = new AtomicLong(0);

    public AtomicLong failedJobsCount = new AtomicLong(0);

    public AtomicLong completedJobsTotalDurationNano = new AtomicLong(0);

    private NCachedValue<NWorkBalancerHostLoadMetrics> loadMetricsNCachedValue;
    public final List<NWorkBalancerRunningJob> runningJobs = Collections.synchronizedList(new ArrayList<>());

    public NWorkBalancerWorkerLoadImpl(NWorkBalancerWorkerModel worker,int workerIndex) {
        this.worker = worker;
        this.workerIndex = workerIndex;
        this.loadMetricsNCachedValue = NCachedValue.of(
                () -> {
                    NWorkBalancerHostLoadMetricProvider p = worker.hostLoadMetricsProvider();
                    if (p != null) {
                        NWorkBalancerHostLoadMetrics z = p.resolveDefaultMetrics();
                        if (z != null) {
                            return z.copy();
                        }
                    }
                    return new NWorkBalancerHostLoadMetrics();
                }
        );
    }

    public int getWorkerIndex() {
        return workerIndex;
    }

    @Override
    public NCachedValue<NWorkBalancerHostLoadMetrics> hostLoadMetrics() {
        return loadMetricsNCachedValue;
    }


    @Override
    public long activeJobsCount() {
        return activeJobsCount.get(); // not tracked yet
    }

    @Override
    public long totalJobsCount() {
        return totalJobsCount.get();
    }

    @Override
    public long succeededJobCount() {
        return succeededJobCount.get();
    }

    @Override
    public long failedJobsCount() {
        return failedJobsCount.get();
    }

    @Override
    public long activeJobsTotalDurationNano() {
        long now = System.nanoTime();
        long total = 0;
        synchronized (runningJobs) {
            for (NWorkBalancerRunningJob job : runningJobs) {
                long start = job.startTimeNano();
                long end = job.isRunning() ? now : job.endTimeNano();
                total += (end - start);
            }
        }
        return total;
    }

    @Override
    public long completedJobsTotalDurationNano() {
        return completedJobsTotalDurationNano.get();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append("worker:").append(worker.name() != null ? worker.name() : "\"null\"");

        long aCount = activeJobsCount.get();
        if (aCount > 0) sb.append(",activeJobsCount:").append(aCount);

        long tCount = totalJobsCount.get();
        if (tCount > 0) sb.append(",totalJobsCount:").append(tCount);

        long sCount = succeededJobCount.get();
        if (sCount > 0) sb.append(",succeededJobCount:").append(sCount);

        long fCount = failedJobsCount.get();
        if (fCount > 0) sb.append(",failedJobsCount:").append(fCount);

        long totalDuration = completedJobsTotalDurationNano.get();
        if (totalDuration > 0) sb.append(",completedJobsTotalDuration:").append(NDuration.ofNanos(totalDuration));

        long activeDuration = activeJobsTotalDurationNano();
        if (activeDuration > 0) sb.append(",activeJobsTotalDuration:").append(NDuration.ofNanos(activeDuration));

        // include host load metrics
        NWorkBalancerHostLoadMetrics metrics = hostLoadMetrics().get();
        if (metrics != null) {
            sb.append(",hostLoadMetrics:").append(metrics.toString());
        }

        // optionally include running jobs if not empty
        synchronized (runningJobs) {
            if (!runningJobs.isEmpty()) {
                sb.append(",runningJobs:[");
                boolean first = true;
                for (NWorkBalancerRunningJob job : runningJobs) {
                    if (!first) sb.append(",");
                    sb.append("{jobId:").append(job.jobId())
                            .append(",jobName:").append(job.jobName())
                            .append(",workerName:").append(job.workerName())
                            .append(",duration:").append(job.duration())
                            .append(",running:").append(job.isRunning())
                            .append("}");
                    first = false;
                }
                sb.append("]");
            }
        }

        sb.append("}");
        return sb.toString();
    }
}
