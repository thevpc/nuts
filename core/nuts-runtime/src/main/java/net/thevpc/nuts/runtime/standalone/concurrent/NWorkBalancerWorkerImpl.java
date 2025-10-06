package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NWorkBalancerHostLoadMetricProvider;
import net.thevpc.nuts.concurrent.NWorkBalancerWorker;
import net.thevpc.nuts.concurrent.NWorkBalancerWorkerModel;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NOptional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NWorkBalancerWorkerImpl implements NWorkBalancerWorker {
    private final NWorkBalancerWorkerModel selectedWorker;

    public NWorkBalancerWorkerImpl(NWorkBalancerWorkerModel selectedWorker) {
        this.selectedWorker = selectedWorker;
    }

    @Override
    public String getName() {
        return selectedWorker.getName();
    }

    @Override
    public NWorkBalancerHostLoadMetricProvider getHostLoadMetricsProvider() {
        return selectedWorker.getHostLoadMetricsProvider();
    }

    @Override
    public Map<String, NElement> getOptions() {
        if (selectedWorker.getOptions() == null) {
            return new HashMap<>();
        }
        return Collections.unmodifiableMap(selectedWorker.getOptions());
    }

    @Override
    public float getWeight() {
        return selectedWorker.getWeight();
    }

    @Override
    public NOptional<NElement> getOption(String name) {
        if (selectedWorker.getOptions() == null) {
            return NOptional.ofNamedEmpty(name);
        }
        return NOptional.ofNamed(selectedWorker.getOptions().get(name), name);
    }
}
