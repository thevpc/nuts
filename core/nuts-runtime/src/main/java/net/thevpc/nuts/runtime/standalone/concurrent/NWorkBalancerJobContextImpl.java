package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NWorkBalancerJobContext;
import net.thevpc.nuts.concurrent.NWorkBalancerModel;
import net.thevpc.nuts.concurrent.NWorkBalancerWorker;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NOptional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class NWorkBalancerJobContextImpl implements NWorkBalancerJobContext {
    private final NWorkBalancerModel model;
    private final NWorkBalancerWorkerImpl worker;
    private final String jobName;
    private final String jobId;
    private final int workerIndex;

    public NWorkBalancerJobContextImpl(String jobId, String jobName, NWorkBalancerWorkerImpl worker, int workerIndex, NWorkBalancerModel model) {
        this.worker = worker;
        this.model = model;
        this.jobName = jobName;
        this.jobId = jobId;
        this.workerIndex = workerIndex;
    }

    @Override
    public int workerIndex() {
        return workerIndex;
    }

    @Override
    public int workersCount() {
        return model.workers().size();
    }

    @Override
    public String jobId() {
        return jobId;
    }

    public String jobName() {
        return jobName;
    }

    public String workerName() {
        return worker.name();
    }

    @Override
    public NWorkBalancerWorker worker() {
        return worker;
    }

    @Override
    public Map<String, NElement> options() {
        if (model.options() == null) {
            return new HashMap<>();
        }
        return Collections.unmodifiableMap(model.options());
    }

    @Override
    public NOptional<NElement> getOption(String name) {
        if (model.options() == null) {
            return NOptional.ofNamedEmpty(name);
        }
        return NOptional.ofNamed(model.options().get(name), name);
    }
}
