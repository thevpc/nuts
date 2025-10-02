package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NCallable;

import java.util.List;

public interface NWorkBalancerCallStrategy<T> extends NCallable<T> {
    void onStartCall(String name);
    void onEndCall(String name,Throwable error);
    String selectCall(List<String> all);
}
