package net.thevpc.nuts.runtime.bundles.mvn;

public interface PomIdFilter {

    boolean accept(PomId id);
}
