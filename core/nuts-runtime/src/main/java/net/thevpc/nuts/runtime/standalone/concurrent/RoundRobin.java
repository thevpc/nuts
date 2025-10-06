package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NWorkBalancerStrategy;
import net.thevpc.nuts.concurrent.NWorkBalancerStrategyContext;
import net.thevpc.nuts.concurrent.NWorkBalancerStrategyEvent;
import net.thevpc.nuts.concurrent.NWorkBalancerWorker;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class RoundRobin implements NWorkBalancerStrategy {
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public void onStartCall(NWorkBalancerStrategyEvent event) {

    }

    @Override
    public void onEndCall(NWorkBalancerStrategyEvent event) {

    }

    @Override
    public String selectWorker(NWorkBalancerStrategyContext context) {
        List<NWorkBalancerWorker> workers = context.getWorkers();
        if (workers.isEmpty()) {
            throw new IllegalStateException("No workers available for load balancing");
        }

        // compute total weight
        float totalWeight = 0f;
        for (NWorkBalancerWorker w : workers) {
            totalWeight += Math.max(0, w.getWeight()); // negative = 0
        }

        // fallback: all weights 0 -> default round robin
        if (totalWeight <= 0) {
            int index = Math.floorMod(counter.getAndIncrement(), workers.size());
            return workers.get(index).getName();
        }

        // select worker based on weighted position
        float position = counter.getAndIncrement() % totalWeight;
        float cumulative = 0f;
        for (NWorkBalancerWorker w : workers) {
            cumulative += Math.max(0, w.getWeight());
            if (position < cumulative) {
                return w.getName();
            }
        }

        // should never reach here
        return workers.get(workers.size() - 1).getName();
    }
}
