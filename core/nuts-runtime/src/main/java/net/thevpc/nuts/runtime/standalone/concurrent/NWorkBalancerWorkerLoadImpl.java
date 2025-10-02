package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NWorkBalancerHostLoadProvider;
import net.thevpc.nuts.concurrent.NWorkBalancerWorkerLoad;
import net.thevpc.nuts.concurrent.NWorkBalancerWorkerModel;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NOptional;

import java.util.Map;

class NWorkBalancerWorkerLoadImpl implements NWorkBalancerWorkerLoad {
    final NWorkBalancerHostLoadProvider p;

    public NWorkBalancerWorkerLoadImpl(NWorkBalancerWorkerModel worker) {
        p = worker.getLoadSupplier();
    }

    @Override
    public NOptional<Float> hostLoadFactor() {
        return NOptional.of(p.resolveHostLoad());
    }

    @Override
    public NOptional<Float> hostCpuLoad() {
        return NOptional.of(p.resolveHostCpuLoad());
    }

    @Override
    public NOptional<Float> hostMemoryLoad() {
        return NOptional.of(p.resolveHostMemoryLoad());
    }

    @Override
    public NOptional<Long> hostLatency() {
        return NOptional.of(p.resolveHostLatency());
    }

    @Override
    public long hostLoadLastUpdateNano() {
        return System.nanoTime(); // placeholder, could be cached
    }

    @Override
    public void refreshHostLoad() {
        // no-op, dynamic from provider
    }

    @Override
    public long activeJobsCount() {
        return 0; // not tracked yet
    }

    @Override
    public long totalJobsCount() {
        return 0;
    }

    @Override
    public long succeededJobCount() {
        return 0;
    }

    @Override
    public long failedJobsCount() {
        return 0;
    }

    @Override
    public long activeJobsTotalDurationNano() {
        return 0;
    }

    @Override
    public long completedJobsTotalDurationNano() {
        return 0;
    }

    @Override
    public Map<String, NElement> hostMetrics() {
        return p.resolveMetrics();
    }
}
