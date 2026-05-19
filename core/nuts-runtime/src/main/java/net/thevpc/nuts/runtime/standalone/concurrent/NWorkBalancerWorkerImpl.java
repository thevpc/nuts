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
    public String name() {
        return selectedWorker.name();
    }

    @Override
    public NWorkBalancerHostLoadMetricProvider hostLoadMetricsProvider() {
        return selectedWorker.hostLoadMetricsProvider();
    }

    @Override
    public Map<String, NElement> options() {
        if (selectedWorker.options() == null) {
            return new HashMap<>();
        }
        return Collections.unmodifiableMap(selectedWorker.options());
    }

    @Override
    public float weight() {
        return selectedWorker.weight();
    }

    @Override
    public NOptional<NElement> getOption(String name) {
        if (selectedWorker.options() == null) {
            return NOptional.ofNamedEmpty(name);
        }
        return NOptional.ofNamed(selectedWorker.options().get(name), name);
    }
}
