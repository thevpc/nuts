package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

class PowerOfTwoLoad implements NWorkBalancerStrategy {
    private final AtomicInteger rrCounter = new AtomicInteger(0);
    private final Random random = new Random();

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

        // Pick two random eligible workers
        NWorkBalancerWorker w1 = pickRandomWorker(workers);
        NWorkBalancerWorker w2 = pickRandomWorker(workers);

        // Compute effective load = hostLoad / weight
        double load1 = resolveEffectiveLoad(w1, context);
        double load2 = resolveEffectiveLoad(w2, context);

        // Choose the one with lower effective load, break tie with round-robin
        if (Double.compare(load1, load2) < 0) {
            return w1.getName();
        } else if (Double.compare(load2, load1) < 0) {
            return w2.getName();
        } else {
            return rrCounter.getAndIncrement() % 2 == 0 ? w1.getName() : w2.getName();
        }
    }

    private NWorkBalancerWorker pickRandomWorker(List<NWorkBalancerWorker> workers) {
        NWorkBalancerWorker w;
        int tries = 0;
        do {
            w = workers.get(random.nextInt(workers.size()));
            tries++;
        } while (w.getWeight() <= 0 && tries < 10);
        if (w.getWeight() <= 0) {
            // fallback: pick any even if weight <= 0
            w = workers.get(random.nextInt(workers.size()));
        }
        return w;
    }

    private double resolveEffectiveLoad(NWorkBalancerWorker worker, NWorkBalancerStrategyContext context) {
        double loadValue = 0.0;
        NWorkBalancerWorkerLoad load = context.getWorkerLoad(worker.getName()).orNull();
        if (load != null) {
            float hostLoad = load.hostLoadMetrics().get().getHostLoadFactor();
            if (!Float.isNaN(hostLoad)) {
                loadValue = hostLoad;
            }
        }
        float weight = worker.getWeight() > 0 ? worker.getWeight() : 1.0f;
        return loadValue / weight;
    }
}
