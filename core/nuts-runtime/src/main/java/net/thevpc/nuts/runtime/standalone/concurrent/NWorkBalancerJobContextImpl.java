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

    public NWorkBalancerJobContextImpl(String jobId, String jobName, NWorkBalancerWorkerImpl worker, NWorkBalancerModel model) {
        this.worker = worker;
        this.model = model;
        this.jobName = jobName;
        this.jobId = jobId;
    }

    @Override
    public String getJobId() {
        return jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public String getWorkerName() {
        return worker.getName();
    }

    @Override
    public NWorkBalancerWorker getWorker() {
        return worker;
    }

    @Override
    public Map<String, NElement> getOptions() {
        if (model.getOptions() == null) {
            return new HashMap<>();
        }
        return Collections.unmodifiableMap(model.getOptions());
    }

    @Override
    public NOptional<NElement> getOption(String name) {
        if (model.getOptions() == null) {
            return NOptional.ofNamedEmpty(name);
        }
        return NOptional.ofNamed(model.getOptions().get(name), name);
    }
}
