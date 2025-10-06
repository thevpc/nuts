package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class LeastLoad implements NWorkBalancerStrategy {
    private final AtomicInteger rrCounter = new AtomicInteger(0);

    @Override
    public void onStartCall(NWorkBalancerStrategyEvent event) {
        // no-op
    }

    @Override
    public void onEndCall(NWorkBalancerStrategyEvent event) {
        // no-op
    }

    @Override
    public String selectWorker(NWorkBalancerStrategyContext context) {
        List<NWorkBalancerWorker> workers = context.getWorkers();
        if (workers.isEmpty()) {
            throw new IllegalStateException("No workers available");
        }

        // Track best worker based on effective load
        double bestLoad = Double.MAX_VALUE;
        List<NWorkBalancerWorker> bestWorkers = new ArrayList<>();

        for (NWorkBalancerWorker w : workers) {
            float weight = w.getWeight();
            if (weight <= 0) {
                // weight <= 0 means excluded from balancing
                continue;
            }

            double load = resolveWorkerLoad(w.getName(), context);
            double effectiveLoad = load / weight;

            if (effectiveLoad < bestLoad) {
                bestLoad = effectiveLoad;
                bestWorkers.clear();
                bestWorkers.add(w);
            } else if (Double.compare(effectiveLoad, bestLoad) == 0) {
                bestWorkers.add(w);
            }
        }

        if (bestWorkers.isEmpty()) {
            throw new IllegalStateException("No eligible workers found (all weights <= 0?)");
        }

        // Break ties deterministically with round robin among best
        int idx = Math.floorMod(rrCounter.getAndIncrement(), bestWorkers.size());
        return bestWorkers.get(idx).getName();
    }

    private double resolveWorkerLoad(String workerId, NWorkBalancerStrategyContext context) {
        NWorkBalancerWorkerLoad load = context.getWorkerLoad(workerId).orNull();
        if (load != null) {
            float hostLoad = load.hostLoadMetrics().get().getHostLoadFactor();
            if (!Float.isNaN(hostLoad)) {
                return hostLoad;
            }
        }
        // fallback: treat unknown load as 0.0 (idle)
        return 0.0;
    }
}
