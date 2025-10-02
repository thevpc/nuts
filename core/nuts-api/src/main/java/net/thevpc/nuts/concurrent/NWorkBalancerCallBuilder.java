package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NCallable;

public interface NWorkBalancerCallBuilder<T> {
    NWorkBalancerCallBuilder<T> add(String workerName, NCallable<T> callable);

    NWorkBalancerCallBuilder<T> remove(String workerName);

    NWorkBalancerCallBuilder<T> setStrategy(String strategy);
    NWorkBalancerCallBuilder<T> setOption(String workerName, String optionName, NElement optionValue);
}
