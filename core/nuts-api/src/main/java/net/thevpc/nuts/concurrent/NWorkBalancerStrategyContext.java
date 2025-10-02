package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Map;

/**
 * @since 0.8.7
 */
public interface NWorkBalancerStrategyContext {
    List<String> workers();

    NOptional<NWorkBalancerWorkerLoad> getWorkerLoad(String workerName);

    Map<String, NWorkBalancerWorkerLoad> getWorkerLoad();

    NOptional<NElement> getOption(String name);

    NOptional<NElement> getWorkerOption(String workerName, String name);

    Map<String, NElement> getWorkerOptions(String workerName);

    Map<String, NElement> getOptions();

    NOptional<NElement> getWorkerVar(String workerName, String name);

    NOptional<NElement> getVar(String name);

    NWorkBalancerStrategyContext setWorkerVar(String workerName,String name, NElement value);

    NWorkBalancerStrategyContext setVar(String name, NElement value);

}
